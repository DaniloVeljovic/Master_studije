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
import scala.Array;
import scala.Tuple2;
import tweet.Parse;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class CitiBikeAnalyzer {
    private static final Function2<Integer, Integer, Integer> SUM_REDUCER = Integer::sum;

    private static final Log LOGGER = LogFactory.getLog(CitiBikeAnalyzer.class);

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

        //test case: End station Washington Park
        System.out.println("Washington park dest: ");

        JavaRDD<Record> recordsWhoseEndStationIsWashingtonPark = rdd_records.filter(t -> t.getEndStationName().equals("Washington Park"));

        recordsWhoseEndStationIsWashingtonPark.collect().forEach(System.out::println);

        JavaRDD<Integer> tripDurationsToWashingtonPark = recordsWhoseEndStationIsWashingtonPark.map(Record::getTripDuration);

        Integer min = tripDurationsToWashingtonPark.min(Comparator.naturalOrder());

        Integer max = tripDurationsToWashingtonPark.max(Comparator.naturalOrder());

        long avg = tripDurationsToWashingtonPark.reduce(Integer::sum) / tripDurationsToWashingtonPark.count();

        System.out.println("Shortest trip to Washington park, longest trip to Washington park and average length of trips to Washington park: "
                + min + ", " + max + ", " + avg);

        //task 1: number of departures from a station
        JavaPairRDD<String, Integer> numberOfDeparturesFromEachStation = rdd_records
                .mapToPair(i -> new Tuple2<>(i.getStartStationName(), 1))
                .reduceByKey(Integer::sum);

        numberOfDeparturesFromEachStation.foreach(System.out::println);

        Tuple2<String, Integer> minStationAndNumberOfDepartures = numberOfDeparturesFromEachStation.min((a, b) -> Math.min(a._2(), b._2()));

        Tuple2<String, Integer> maxStationAndNumberOfDepartures = numberOfDeparturesFromEachStation.max((a, b) -> Math.max(a._2(), b._2()));

        Integer reduce = numberOfDeparturesFromEachStation.map(i -> i._2).reduce(Integer::sum);
        double avgNumOfDeparturesFromEachStations = reduce / (double) numberOfDeparturesFromEachStation.count();

        System.out.println("The station with the most departures is "
                + maxStationAndNumberOfDepartures._1()+", and the departure number is " + maxStationAndNumberOfDepartures._2());

        System.out.println("The station with the least departures is "
                + minStationAndNumberOfDepartures._1()+", and the departure number is " + minStationAndNumberOfDepartures._2());

        System.out.println("The average number of departures is " + avgNumOfDeparturesFromEachStations);

        //task 2: number of arrivals to a station add date
        JavaPairRDD<String, Integer> numberOfArrivalsToEachStation = rdd_records
                .mapToPair(i -> new Tuple2<>(i.getEndStationName(), 1))
                .reduceByKey(Integer::sum);

        numberOfArrivalsToEachStation.foreach(System.out::println);

        JavaPairRDD<String, Integer> minStationsAndNumberOfArrivals = numberOfArrivalsToEachStation.reduceByKey(Math::min);

        JavaPairRDD<String, Integer> maxStationsAndNumberOfArrivals = numberOfArrivalsToEachStation.reduceByKey(Math::max);

        Integer sum = numberOfArrivalsToEachStation.map(i -> i._2).reduce(Integer::sum);
        double avgNumOfArrivalsToEachStations = sum / (double) numberOfArrivalsToEachStation.count();

        Tuple2<String, Integer> minStationAndNumberOfArrivals = minStationsAndNumberOfArrivals.min((a, b) -> Math.min(a._2(), b._2()));
        Tuple2<String, Integer> maxStationAndNumberOfArrivals = maxStationsAndNumberOfArrivals.max((a, b) -> Math.max(a._2(), b._2()));

        System.out.println("The station with the most arrivals is "
                + maxStationAndNumberOfArrivals._1()+", and the departure number is " + maxStationAndNumberOfArrivals._2());

        System.out.println("The station with the least departures is "
                + minStationAndNumberOfArrivals._1()+", and the departure number is " + minStationAndNumberOfArrivals._2());

        System.out.println("The average number of departures is " + avgNumOfArrivalsToEachStations);


        //task 3: number departures from each station at a certain time for every day of the week - may have null fields, should be checked
        //TODO: Same can be done for arrivals

        JavaPairRDD<String, Integer> numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek = rdd_records
                .filter(t -> !t.getStartTime().equals(OffsetDateTime.MAX))
                .mapToPair(t -> new Tuple2<>(t.getStartStationName(), t.getStartTime()))
                .filter(t -> t._2().getHour() == 12)
                .mapToPair(t -> new Tuple2<>(t._1() + " " + t._2().getDayOfWeek().name(), 1))
                .reduceByKey(Integer::sum);

        numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek.foreach(System.out::println);

        Tuple2<String, Integer> minDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek =
                numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek
                .min((a, b) -> Math.min(a._2(), b._2()));

        Tuple2<String, Integer> maxDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek =
                numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek
                .min((a, b) -> Math.max(a._2(), b._2()));

        Integer sumDep = numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek.map(i -> i._2).reduce(Integer::sum);
        double avgDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek = sumDep / (double) numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek.count();

        System.out.println("The station with the most departures is "
                + maxDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek._1()+", and the departure number is "
                + maxDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek._2());

        System.out.println("The station with the least departures is "
                + minDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek._1()+", and the departure number is "
                + minDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek._2());

        System.out.println("The average number of departures is " + avgDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek);

        //task 4: most popular bike for each station and length of each trip (get consumer info) at daytime ( > 17:00)
        JavaRDD<Record> recordsWhoseStartTimeIsGreaterThen17 = rdd_records
                .filter(t -> !t.getStartTime().equals(OffsetDateTime.MAX))
                .filter(t -> t.getStartTime().getHour() < 17);

        JavaPairRDD<String, Integer> mostPopularDayTimeBikesForEachStation = recordsWhoseStartTimeIsGreaterThen17
                .mapToPair(t -> new Tuple2<>(t.getStartStationName() + " " + t.getBikeId(), 1))
                .reduceByKey(Integer::sum)
                .reduceByKey((a, b) -> a > b ? a : b);

        mostPopularDayTimeBikesForEachStation.foreach(System.out::println);

        //TODO: ADD MIN, MAX AND AVG VALUES

        //task 5: number departures from each station at noon where the start station is east of the Lafayette Ave & St James Pl,
        //id = 293, lat = 40.73020660529954, long = -73.99102628231049)
        JavaPairRDD<String, Integer> eastOfLafayetteDepartures = rdd_records
                .filter(t -> !t.getStartTime().equals(OffsetDateTime.MAX))
                .filter(t -> t.getStartStationLongitude().compareTo(BigDecimal.valueOf(-73.99102628231049)) > 0)
                .filter(t -> t.getStartTime().getHour() == 12)
                .mapToPair(t -> new Tuple2<>(t.getStartStationName(), 1))
                .reduceByKey(Integer::sum);

        System.out.println(eastOfLafayetteDepartures);

        //task 6: number departures from each station at noon where the start station is south of Lafayette Ave & St James Pl

        JavaPairRDD<String, Integer> southOfLafayetteDepartures = rdd_records
                .filter(t -> !t.getStartTime().equals(OffsetDateTime.MAX))
                .filter(t -> t.getStartStationLatitude().compareTo(BigDecimal.valueOf(40.73020660529954)) < 0)
                .filter(t -> t.getStartTime().getHour() == 12)
                .mapToPair(t -> new Tuple2<>(t.getStartStationName(), 1))
                .reduceByKey(Integer::sum);

        System.out.println(southOfLafayetteDepartures);

        //task 7: get gender of people who rent bikes in the summer at Washington Park (id = 262) (summer = June, July, August)

        List<Integer> summerMonths = Arrays.asList(6, 7, 8);

        JavaPairRDD<Integer, Integer> gendersWhoRentBikesInSummer = rdd_records
                .filter(t -> summerMonths.contains(t.getStartTime().getMonthValue()))
                .filter(t -> t.getStartStationId().equals(262L))
                .mapToPair(t -> new Tuple2<>(t.getGender(), 1))
                .reduceByKey(Integer::sum);

        System.out.println(gendersWhoRentBikesInSummer);

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
