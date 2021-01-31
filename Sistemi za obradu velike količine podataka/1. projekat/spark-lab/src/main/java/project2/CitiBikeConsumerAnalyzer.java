package project2;

import com.datastax.driver.core.Session;
import kafka.loggenerator.SparkStreamingKafkaLogAnalyzer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.sql.sources.In;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import project1.Record;
import scala.Int;
import scala.Tuple2;
import tweet.Parse;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class CitiBikeConsumerAnalyzer {
    private static Function2<Integer, Integer, Integer> SUM_REDUCER = (a, b) -> a + b;

    private static final Log LOGGER = LogFactory.getLog(SparkStreamingKafkaLogAnalyzer.class);

    // Stats will be computed for the last window length of time.
    private static final Duration WINDOW_LENGTH = new Duration(600 * 1000);

    // Stats will be computed every slide interval time.
    private static final Duration SLIDE_INTERVAL = new Duration(300 * 1000);

    public static void main(String[] args) {
        // Set application name
        String appName = "Spark Streaming Kafka";

        CassandraConnector connector = new CassandraConnector();
        connector.connect();
        Session session = connector.getSession();
        connector.createKeyspace("citibike", "SimpleStrategy", 1);
        connector.createTable("citibike.citibike");
        connector.emptyTable();

        //empty the citibike table


        // Create a Spark Context.
        SparkConf conf = new SparkConf()
                .setAppName(appName)
                .setMaster("local[*]")
                .set("spark.executor.memory", "1g");
        JavaSparkContext sc = new JavaSparkContext(conf);

        // This sets the update window to be every 10 seconds.
        JavaStreamingContext jssc = new JavaStreamingContext(sc, SLIDE_INTERVAL);

        String zkQuorum = "localhost:2181";
        String group = "spark-streaming-sample-groupid";
        String strTopics = "spark-streaming-sample-topic";
        int numThreads = 2;

        Map<String, Integer> topicMap = new HashMap<String, Integer>();
        String[] topics = strTopics.split(",");
        for (String topic : topics) {
            topicMap.put(topic, numThreads);
        }

        JavaPairReceiverInputDStream<String, String> logDataDStream =
                KafkaUtils.createStream(jssc, zkQuorum, group, topicMap);
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("Received DStream connecting to zookeeper " + zkQuorum + " group " + group + " topics" +
                topicMap);
        System.out.println("logDataDStream: " + logDataDStream);

        JavaDStream<Record> accessLogDStream = logDataDStream.map(
                new Function<Tuple2<String, String>, Record>() {
                    public Record call(Tuple2<String, String> message) {
                        String strLogMsg = message._2();
                        return Parse.parseStringToRecord(strLogMsg);
                    }
                }
        );
        accessLogDStream.print();

        JavaDStream<Record> windowDStream = accessLogDStream.window(
                WINDOW_LENGTH, SLIDE_INTERVAL);

        windowDStream.foreachRDD(new Function<JavaRDD<Record>, Void>() {
            @Override
            public Void call(JavaRDD<Record> rdd_records) throws IOException {
                if (rdd_records.count() == 0) {
                    LOGGER.debug("No access logs in this time interval");
                    return null;
                }

                JavaRDD<Record> recordsWhoseEndStationIsWashingtonPark = rdd_records.filter(t -> t.getEndStationName().equals("Washington Park"));

                if (!recordsWhoseEndStationIsWashingtonPark.isEmpty()) {
                    JavaRDD<Integer> tripDurationsToWashingtonPark = recordsWhoseEndStationIsWashingtonPark.map(Record::getTripDuration);

                    Integer min = tripDurationsToWashingtonPark.min(Comparator.naturalOrder());

                    Integer max = tripDurationsToWashingtonPark.max(Comparator.naturalOrder());

                    Long avg = tripDurationsToWashingtonPark.reduce(SUM_REDUCER) / tripDurationsToWashingtonPark.count();

                    String s = "Shortest trip to Washington park, longest trip to Washington park and average length of trips to Washington park: "
                            + min + ", " + max + ", " + avg;

                    connector.insertInto(s);

                    JavaPairRDD<String, Integer> userTypeRDD = recordsWhoseEndStationIsWashingtonPark.mapToPair(t -> new Tuple2<>(t.getUserType(), 1)).reduceByKey(Integer::sum);

                    Integer min1 = userTypeRDD.map(Tuple2::_2).min(Comparator.naturalOrder());

                    Integer max1 = userTypeRDD.map(Tuple2::_2).max(Comparator.naturalOrder());

                    Long avg1 = userTypeRDD.map(Tuple2::_2).reduce(SUM_REDUCER) / userTypeRDD.count();
                }


                //task 1: all stations for which there are more than 5 departures
                JavaPairRDD<String, Integer> numberOfDeparturesFromEachStation = rdd_records
                        .mapToPair(i -> new Tuple2<>(i.getStartStationName(), 1))
                        .reduceByKey(Integer::sum)
                        .filter(t -> t._2 > 5);

                if (!numberOfDeparturesFromEachStation.isEmpty()) {
                    Integer maxStationAndNumberOfDepartures = numberOfDeparturesFromEachStation.map(t -> t._2).max(Comparator.naturalOrder());

                    Integer minStationAndNumberOfDepartures = numberOfDeparturesFromEachStation.map(t -> t._2).min(Comparator.naturalOrder());

                    Integer reduce = numberOfDeparturesFromEachStation.map(i -> i._2).reduce(Integer::sum);
                    double avgNumOfDeparturesFromEachStations = reduce / (double) numberOfDeparturesFromEachStation.count();

                    connector.insertInto("Maximum departures from a station is " + maxStationAndNumberOfDepartures);

                    connector.insertInto("\nMinimum departures from a station is " + minStationAndNumberOfDepartures);

                    connector.insertInto("\nThe average number of departures is " + avgNumOfDeparturesFromEachStations);
                }


                //task 5: number departures from each station at noon where the start station is east of the Lafayette Ave & St James Pl,
                //id = 293, lat = 40.73020660529954, long = -73.99102628231049)
                JavaRDD<Record> eastOfLafayetteDepartures = rdd_records
                        .filter(t -> !t.getStartTime().equals(LocalDateTime.MAX))
                        .filter(t -> t.getStartStationLongitude().compareTo(BigDecimal.valueOf(-73.99102628231049)) > 0);

                if (!eastOfLafayetteDepartures.isEmpty()) {
                    JavaPairRDD<Long, Integer> mostPopularBikesEastOfLafayette = eastOfLafayetteDepartures.mapToPair(t -> new Tuple2<>(t.getBikeId(), 1)).reduceByKey(Integer::sum);

                    Integer mostDepartures = mostPopularBikesEastOfLafayette.map(t -> t._2).max(Comparator.naturalOrder());

                    Integer leastDepartures = mostPopularBikesEastOfLafayette.map(t -> t._2).min(Comparator.naturalOrder());

                    Integer sumDep = mostPopularBikesEastOfLafayette.map(i -> i._2).reduce(Integer::sum);
                    double avgDepartures = sumDep / (double) mostPopularBikesEastOfLafayette.count();

                    connector.insertInto("Maximum number of departures east of Lafayette station " + mostDepartures);

                    connector.insertInto("\nMinimum number of departures east of Lafayette station " + leastDepartures);

                    connector.insertInto("\nAverage number of departures east of Lafayette " + avgDepartures);
                }


                //task 6: number departures from each station at noon where the start station is south of Lafayette Ave & St James Pl

                JavaRDD<Record> southOfLafayetteDepartures = rdd_records
                        .filter(t -> !t.getStartTime().equals(LocalDateTime.MAX))
                        .filter(t -> t.getStartStationLatitude().compareTo(BigDecimal.valueOf(40.73020660529954)) < 0);

                if (!southOfLafayetteDepartures.isEmpty()) {
                    JavaRDD<Integer> tripDurations = southOfLafayetteDepartures.map(Record::getTripDuration);

                    Integer mostSouthDepartures = tripDurations.max(Comparator.naturalOrder());

                    Integer leastSouthDepartures = tripDurations.min(Comparator.naturalOrder());

                    double avgNumSouthDep = tripDurations.reduce(Integer::sum) / (double) tripDurations.count();

                    connector.insertInto("Maximum number of departures south of Lafayette station " + mostSouthDepartures);

                    connector.insertInto("\nMinimum number of departures south of Lafayette station " + leastSouthDepartures);

                    connector.insertInto("\nAverage number of departures south of Lafayette " + avgNumSouthDep);
                }

                //b. top N start locations and top N end locations and top N bikes

                JavaPairRDD<String, Integer> mostCommonStartEndStation = rdd_records.mapToPair(t -> new Tuple2<>(t.getStartStationName() + " " + t.getEndStationName(), 1)).reduceByKey(Integer::sum);

                JavaPairRDD<String, Integer> mostCommonStartStation = rdd_records.mapToPair(t -> new Tuple2<>(t.getStartStationName(), 1)).reduceByKey(Integer::sum);

                JavaPairRDD<String, Integer> mostCommonEndStation = rdd_records.mapToPair(t -> new Tuple2<>(t.getEndStationName(), 1)).reduceByKey(Integer::sum);

                JavaPairRDD<Long, Integer> mostPopularBikes = rdd_records.mapToPair(t -> new Tuple2<>(t.getBikeId(), 1)).reduceByKey(Integer::sum);

                return null;
            }
        });

        // Start the streaming server.
        jssc.start(); // Start the computation
        jssc.awaitTermination(); // Wait for the computation to terminate
    }
}
