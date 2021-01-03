package project1;

import kafka.loggenerator.ApacheAccessLog;
import kafka.loggenerator.SparkStreamingKafkaLogAnalyzer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.In;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;
import scala.Tuple2;
import tweet.Parse;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CitiBikeAnalyzer {
    private static Function2<Integer, Integer, Integer> SUM_REDUCER = (a, b) -> a + b;

    private static final Log LOGGER = LogFactory.getLog(SparkStreamingKafkaLogAnalyzer.class);

    // Stats will be computed for the last window length of time.
    private static final Duration WINDOW_LENGTH = new Duration(30 * 1000);

    // Stats will be computed every slide interval time.
    private static final Duration SLIDE_INTERVAL = new Duration(10 * 1000);

    public static void main(String[] args) {
        // Set application name
        String appName = "Spark Streaming Kafka Sample";

        // Create a Spark Context.
        SparkConf conf = new SparkConf()
                .setAppName(appName)
                .setMaster("local[*]")
                .set("spark.executor.memory", "1g");
        JavaSparkContext sc = new JavaSparkContext(conf);

        //JavaRDD<String> data = sc.textFile("path/input.csv");
        //JavaSQLContext sqlContext = new JavaSQLContext(sc); // For previous versions
        //SQLContext sqlContext = new SQLContext(sc); // In Spark 1.3 the Java API and Scala API have been unified


        JavaRDD<Record> rdd_records = sc.textFile("hdfs://localhost:9000/inputs/201306-citibike-tripdata.csv").map(Parse::parseStringToRecord);

        System.out.println("Washington park dest: ");

        JavaRDD<Record> recordsWhoseEndStationIsWashingtonPark = rdd_records.filter(t -> t.getEndStationName().equals("Washington Park"));
        recordsWhoseEndStationIsWashingtonPark.collect().forEach(System.out::println);

        JavaRDD<Integer> tripDurationsToWashingtonPark = recordsWhoseEndStationIsWashingtonPark.map(Record::getTripDuration);

        Integer min = tripDurationsToWashingtonPark.min(Comparator.naturalOrder());

        Integer max = tripDurationsToWashingtonPark.max(Comparator.naturalOrder());

        Long avg = tripDurationsToWashingtonPark.reduce(SUM_REDUCER) / tripDurationsToWashingtonPark.count();

        System.out.println("Min, max and average: " + min + ", " + max + ", " + avg);

        /*JavaPairRDD<String, Tuple2<Long, Integer>> records_JPRDD =
                rdd_records.mapToPair((PairFunction<Record, String, Tuple2<Long, Integer>>) record -> {
                    Tuple2<String, Tuple2<Long, Integer>> t2 =
                            new Tuple2<String, Tuple2<Long, Integer>>(
                                    record.department + record.designation + record.state,
                                    new Tuple2<Long, Integer>(record.costToCompany, 1)
                            );
                    return t2;
                });

        JavaPairRDD<String, Tuple2<Long, Integer>> final_rdd_records =
                records_JPRDD.reduceByKey(new Function2<Tuple2<Long, Integer>, Tuple2<Long,
                        Integer>, Tuple2<Long, Integer>>() {
                    public Tuple2<Long, Integer> call(Tuple2<Long, Integer> v1,
                                                      Tuple2<Long, Integer> v2) throws Exception {
                        return new Tuple2<Long, Integer>(v1._1 + v2._1, v1._2 + v2._2);
                    }
                });*/

        /*JavaDStream<ApacheAccessLog> accessLogDStream = logDataDStream.map(
                new Function<Tuple2<String, String>, ApacheAccessLog>() {
                    public ApacheAccessLog call(Tuple2<String, String> message) {
                        String strLogMsg = message._2();
                        return ApacheAccessLog.parseFromLogLine(strLogMsg);
                    }
                }
        );
        accessLogDStream.print();

        JavaDStream<ApacheAccessLog> windowDStream = accessLogDStream.window(
                WINDOW_LENGTH, SLIDE_INTERVAL);

        windowDStream.foreachRDD(new Function<JavaRDD<ApacheAccessLog>, Void>() {
            @Override
            public Void call(JavaRDD<ApacheAccessLog> accessLogs) {
                if (accessLogs.count() == 0) {
                    LOGGER.debug("No access logs in this time interval");
                    return null;
                }

                // Calculate statistics based on the content size.
                JavaRDD<Long> contentSizes = accessLogs.map(ApacheAccessLog::getContentSize).cache();

                Long min = contentSizes.min(Comparator.naturalOrder());
                Long max = contentSizes.max(Comparator.naturalOrder());
                Long avg = contentSizes.reduce(SUM_REDUCER) / contentSizes.count();

                System.out.println("Web request content size statistics: Min=" + min + ", Max=" + max + ", Avg=" + avg);

                // Compute Response Code to Count.
                *//*List<Tuple2<Integer, Long>> responseCodeToCount = accessLogs
                        .mapToPair(Functions.GET_RESPONSE_CODE)
                        .reduceByKey(Functions.SUM_REDUCER).take(100);
                LOGGER.debug("Response code counts: " + responseCodeToCount);

                // Any IPAddress that has accessed the server more than
                // 10 times.
                List<String> ipAddresses = accessLogs
                        .mapToPair(Functions.GET_IP_ADDRESS)
                        .reduceByKey(Functions.SUM_REDUCER)
                        .filter(Functions.FILTER_GREATER_10)
                        .map(Functions.GET_TUPLE_FIRST).take(100);
                LOGGER.debug("IPAddresses > 10 times: " + ipAddresses);

                // Top Endpoints.
                List<Tuple2<String, Long>> topEndpoints = accessLogs
                        .mapToPair(Functions.GET_ENDPOINT)
                        .reduceByKey(Functions.SUM_REDUCER)
                        .top(10,
                                new Functions.ValueComparator<String, Long>(
                                        Functions.LONG_NATURAL_ORDER_COMPARATOR));
                LOGGER.debug("Top Endpoints: " + topEndpoints);*//*

                return null;
            }
        });*/

        // Start the streaming server.
        /*jssc.start(); // Start the computation
        jssc.awaitTermination(); // Wait for the computation to terminate*/
    }
}
