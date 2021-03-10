package project1;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import scala.Tuple2;
import tweet.Parse;

import java.io.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

public class CitiBikeAnalyzer {

    //public static String HDFSPath = "hdfs://namenode:9000/inputs/201306-citibike-tripdata.csv";


    public static void main(String[] args) throws IOException {
        // Set application name
        String appName = "Spark Streaming Kafka Sample";
        String sparkMaster = System.getenv("ES_SPARK_MASTER");
        String HDFSPath = System.getenv("ES_DATA_SOURCE");
        String endStation = System.getenv("END_STATION");
        String stationID = System.getenv("STATION_ID");
        //Arguments:
        /*
        * args[0] = Washington Park
        * args[1] = -73.99102628231049
        * args[2] = 40.73020660529954
        * args[3] = 262
        * */

        // Create a Spark Context.
        SparkConf conf = new SparkConf()
                .setAppName(appName)
                .setMaster(sparkMaster)
                .set("spark.executor.memory", "1g");
        JavaSparkContext sc = new JavaSparkContext(conf);

        JavaRDD<Record> rdd_records = sc.textFile(HDFSPath).map(Parse::parseStringToRecord);

        //test case: End station Washington Park
        System.out.println("Washington park dest: ");

        JavaRDD<Record> recordsWhoseEndStationIsWashingtonPark = rdd_records.filter(t -> t.getEndStationName().equals(endStation));

        recordsWhoseEndStationIsWashingtonPark.collect().forEach(System.out::println);

        JavaRDD<Integer> tripDurationsToWashingtonPark = recordsWhoseEndStationIsWashingtonPark.map(Record::getTripDuration);

        Integer min = tripDurationsToWashingtonPark.min(Comparator.naturalOrder());

        Integer max = tripDurationsToWashingtonPark.max(Comparator.naturalOrder());

        long avg = tripDurationsToWashingtonPark.reduce(Integer::sum) / tripDurationsToWashingtonPark.count();

        String s = "Shortest trip to Washington park, longest trip to Washington park and average length of trips to Washington park: "
                + min + ", " + max + ", " + avg;

        System.out.println(s);

        //task 1: number of departures from a station
        JavaPairRDD<String, Integer> numberOfDeparturesFromEachStation = rdd_records
                .mapToPair(i -> new Tuple2<>(i.getStartStationName(), 1))
                .reduceByKey(Integer::sum);

        System.out.print("\n\n");

        numberOfDeparturesFromEachStation.collect().forEach(t -> {
            System.out.println("Station: " + t._1() + " number of departures " + t._2() + " \n");
        });

        Integer maxStationAndNumberOfDepartures = numberOfDeparturesFromEachStation.map(t -> t._2).max(Comparator.naturalOrder());

        Integer minStationAndNumberOfDepartures = numberOfDeparturesFromEachStation.map(t -> t._2).min(Comparator.naturalOrder());

        Integer reduce = numberOfDeparturesFromEachStation.map(i -> i._2).reduce(Integer::sum);
        double avgNumOfDeparturesFromEachStations = reduce / (double) numberOfDeparturesFromEachStation.count();

        System.out.println("\nMaximum departures from a station is " + maxStationAndNumberOfDepartures);

        System.out.println("Minimum departures from a station is " + minStationAndNumberOfDepartures);

        System.out.println("The average number of departures is " + avgNumOfDeparturesFromEachStations);

        System.out.print("\n\n");

        //task 2: number of arrivals to a station
        JavaPairRDD<String, Integer> numberOfArrivalsToEachStation = rdd_records
                .mapToPair(i -> new Tuple2<>(i.getEndStationName(), 1))
                .reduceByKey(Integer::sum);

        numberOfArrivalsToEachStation.collect().forEach(t -> {
                System.out.println("Station: " + t._1() + " number of arrivals " + t._2() + " \n");
        });

        Integer maxStationAndNumberOfArrivals = numberOfArrivalsToEachStation.map(t -> t._2).max(Comparator.naturalOrder());

        Integer minStationAndNumberOfArrivals = numberOfArrivalsToEachStation.map(t -> t._2).min(Comparator.naturalOrder());

        Integer reduced = numberOfDeparturesFromEachStation.map(i -> i._2).reduce(Integer::sum);
        double avgNumOfArrivalsFromEachStations = reduced / (double) numberOfArrivalsToEachStation.count();

        System.out.println("\nMaximum arrivals from a station is " + maxStationAndNumberOfArrivals);

        System.out.println("Minimum arrivals from a station is " + minStationAndNumberOfArrivals);

        System.out.println("The average number of arrivals is " + avgNumOfArrivalsFromEachStations);

        //task 3: number departures from each station at a certain time for every day of the week - may have null fields, should be checked
        //TODO: Same can be done for arrivals

        JavaPairRDD<String, Integer> numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek = rdd_records
                .filter(t -> !t.getStartTime().equals(LocalDateTime.MAX))
                .mapToPair(t -> new Tuple2<>(t.getStartStationName(), t.getStartTime()))
                .filter(t -> t._2().getHour() == 12)
                .mapToPair(t -> new Tuple2<>(t._1() + " " + t._2().getDayOfWeek().name(), 1))
                .reduceByKey(Integer::sum);

        System.out.print("\n \n");

        numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek.collect().forEach(t -> {
            System.out.println("Station: " + t._1() + " number of arrivals " + t._2() + " at 12 o'clock \n");
        });

        Integer maxNumberOfDeparturesForEachStationAtCertainTimeAndEveryDay =
                numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek.map(t -> t._2).max(Comparator.naturalOrder());

        Integer minNumberOfDeparturesForEachStationAtCertainTimeAndEveryDay =
                numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek.map(t -> t._2).min(Comparator.naturalOrder());

        Integer reduc = numberOfDeparturesFromEachStation.map(i -> i._2).reduce(Integer::sum);
        double avgNumberOfDeparturesForEachStationAtCertainTimeAndEveryDay = reduc
                / (double) numberOfDeparturesFromEachStationAtACertainTimeAndForEveryDayOfTheWeek.count();

        System.out.println("\nMaximum departures from a station is " + maxNumberOfDeparturesForEachStationAtCertainTimeAndEveryDay);

        System.out.println("Minimum departures from a station is " + minNumberOfDeparturesForEachStationAtCertainTimeAndEveryDay);

        System.out.println("The average number of departures is " + avgNumberOfDeparturesForEachStationAtCertainTimeAndEveryDay);

        //task 4: most popular bike for each station and length of each trip (get consumer info) at daytime ( > 17:00)
        JavaRDD<Record> recordsWhoseStartTimeIsGreaterThen17 = rdd_records
                .filter(t -> !t.getStartTime().equals(LocalDateTime.MAX))
                .filter(t -> t.getStartTime().getHour() < 17);

        JavaPairRDD<String, Integer> mostPopularDayTimeBikesForEachStation = recordsWhoseStartTimeIsGreaterThen17
                .mapToPair(t -> new Tuple2<>(t.getStartStationName() + " " + t.getBikeId(), 1))
                .reduceByKey(Integer::sum)
                .reduceByKey((a, b) -> a > b ? a : b);

        System.out.print("\n \n");

        mostPopularDayTimeBikesForEachStation.collect().forEach(t -> {
            System.out.println("Station: " + t._1() + " bike " + t._2() + " \n");
        });

        Integer mostPopular = mostPopularDayTimeBikesForEachStation.map(t -> t._2).max(Comparator.naturalOrder());

        Integer leastPopular = mostPopularDayTimeBikesForEachStation.map(t -> t._2).min(Comparator.naturalOrder());

        Integer sum = mostPopularDayTimeBikesForEachStation.map(i -> i._2).reduce(Integer::sum);
        double avgBike = sum / (double) mostPopularDayTimeBikesForEachStation.count();

        System.out.println("\nMost popular bike out of all stations " + mostPopular);

        System.out.println("Least popular bike out of all stations " + leastPopular);

        System.out.println("Average number of bike rentals " + avgBike);

        //task 5: number departures from each station at noon where the start station is east of the Lafayette Ave & St James Pl,
        //id = 293, lat = 40.73020660529954, long = -73.99102628231049)
        JavaPairRDD<String, Integer> eastOfLafayetteDepartures = rdd_records
                .filter(t -> !t.getStartTime().equals(LocalDateTime.MAX))
                .filter(t -> t.getStartStationLongitude().compareTo(BigDecimal.valueOf(-73.99102628231049)) > 0)
                .filter(t -> t.getStartTime().getHour() == 12)
                .mapToPair(t -> new Tuple2<>(t.getStartStationName(), 1))
                .reduceByKey(Integer::sum);

        System.out.print("\n \n");

        eastOfLafayetteDepartures.collect().forEach(t -> {
            System.out.println("EAST OF LAFAYETTE : Station: " + t._1() + " number of departures " + t._2() + " at 12 o'clock\n");
        });

        Integer mostDepartures = eastOfLafayetteDepartures.map(t -> t._2).max(Comparator.naturalOrder());

        Integer leastDepartures = eastOfLafayetteDepartures.map(t -> t._2).min(Comparator.naturalOrder());

        Integer sumDep = eastOfLafayetteDepartures.map(i -> i._2).reduce(Integer::sum);
        double avgDepartures = sumDep / (double) eastOfLafayetteDepartures.count();

        System.out.println("\nMaximum number of departures east of Lafayette station " + mostDepartures);

        System.out.println("Minimum number of departures east of Lafayette station " + leastDepartures);

        System.out.println("Average number of departures east of Lafayette " + avgDepartures);

        //task 6: number departures from each station at noon where the start station is south of Lafayette Ave & St James Pl

        JavaPairRDD<String, Integer> southOfLafayetteDepartures = rdd_records
                .filter(t -> !t.getStartTime().equals(LocalDateTime.MAX))
                .filter(t -> t.getStartStationLatitude().compareTo(BigDecimal.valueOf(40.73020660529954)) < 0)
                .filter(t -> t.getStartTime().getHour() == 12)
                .mapToPair(t -> new Tuple2<>(t.getStartStationName(), 1))
                .reduceByKey(Integer::sum);

        System.out.print("\n \n");

        southOfLafayetteDepartures.collect().forEach(t -> {
            System.out.println("SOUTH OF LAFAYETTE : Station: " + t._1() + " number of departures " + t._2() + " \n");
        });

        Integer mostSouthDepartures = southOfLafayetteDepartures.map(t -> t._2).max(Comparator.naturalOrder());

        Integer leastSouthDepartures = southOfLafayetteDepartures.map(t -> t._2).min(Comparator.naturalOrder());

        double avgNumSouthDep = southOfLafayetteDepartures.map(i -> i._2).reduce(Integer::sum) / (double) southOfLafayetteDepartures.count();

        System.out.println("\nMaximum number of departures south of Lafayette station " + mostSouthDepartures);

        System.out.println("Minimum number of departures south of Lafayette station " + leastSouthDepartures);

        System.out.println("Average number of departures south of Lafayette " + avgNumSouthDep);

        //task 7: get gender of people who rent bikes in the summer at Washington Park (id = 262) (summer = June, July, August)

        List<Integer> summerMonths = Arrays.asList(6, 7, 8);

        JavaPairRDD<Integer, Integer> gendersWhoRentBikesInSummer = rdd_records
                .filter(t -> summerMonths.contains(t.getStartTime().getMonthValue()))
                .filter(t -> t.getStartStationId().equals(Long.parseLong(stationID)))
                .mapToPair(t -> new Tuple2<>(t.getGender(), 1))
                .reduceByKey(Integer::sum);

        gendersWhoRentBikesInSummer.foreach(t -> System.out.println());

        System.out.print("\n \n");

        gendersWhoRentBikesInSummer.collect().forEach(t -> {
            System.out.println("GENDERS : GENDER : " + t._1() + " number " + t._2() + " \n");
        });

        Integer mostGenders = southOfLafayetteDepartures.map(t -> t._2).max(Comparator.naturalOrder());

        Integer leastGenders = southOfLafayetteDepartures.map(t -> t._2).min(Comparator.naturalOrder());

        double avgGenders = southOfLafayetteDepartures.map(i -> i._2).reduce(Integer::sum) / (double) southOfLafayetteDepartures.count();

        System.out.println("\nGender that rents the most bikes in Washington Park in summer " + mostGenders);

        System.out.println("Gender that rents the least bikes in Washington Park in summer " + leastGenders);

        System.out.println("Average number of bike rentals by gender " + avgGenders);
    }
}
