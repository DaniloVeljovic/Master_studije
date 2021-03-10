/*
package project3;


import jnr.ffi.annotations.In;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.spark.SparkConf;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;


import org.apache.spark.api.java.function.VoidFunction;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.mllib.classification.*;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib.regression.LabeledPoint;
import org.apache.spark.mllib.regression.LinearRegressionModel;
import org.apache.spark.mllib.regression.LinearRegressionWithSGD;

import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import scala.Tuple2;
import tweet.Parse;

import java.io.*;

import java.util.*;

public class CitiBikeConsumerAnalyzer {
    private static Function2<Integer, Integer, Integer> SUM_REDUCER = (a, b) -> a + b;

    // Stats will be computed for the last window length of time.
    private static final Duration WINDOW_LENGTH = new Duration(60 * 1000);

    // Stats will be computed every slide interval time.
    private static final Duration SLIDE_INTERVAL = new Duration(30 * 1000);

    private static final String DATA_FILE = "hdfs://localhost:9000/inputs/train.csv";

    private static final Integer N = 1;

    public static void main(String[] args) throws IOException, InterruptedException {
        // Set application name
        String appName = "Spark Streaming MLib";

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

        //training the model
        JavaRDD<ExtendedRecord> rdd_records = sc.textFile(DATA_FILE).map(Parse::parseStringToExtendedRecord);

        JavaRDD<LabeledPoint> parsedData = rdd_records.map(s -> new LabeledPoint(s.getTripDuration(), Vectors.dense(s.getTest()))); //regression
        LinearRegressionModel model = LinearRegressionWithSGD.train(parsedData.rdd(), 100, 0.00000001); //regression


        */
/*JavaRDD<LabeledPoint> parsedData = rdd_records.map(s -> new LabeledPoint(s.getTestBool(), Vectors.dense(s.getGender(), s.getTripDuration())));

        LogisticRegressionModel logisticRegressionModel = new LogisticRegressionWithLBFGS().setNumClasses(2).run(parsedData.rdd());

        ClassificationModel naiveBayesModel = new NaiveBayes().run(parsedData.rdd());*//*


        JavaDStream<ExtendedRecord> accessLogDStream = logDataDStream.map(
                new Function<Tuple2<String, String>, ExtendedRecord>() {
                    public ExtendedRecord call(Tuple2<String, String> message) {
                        String strLogMsg = message._2();
                        return Parse.parseStringToExtendedRecord(strLogMsg);
                    }
                }
        );

        accessLogDStream.print();

        JavaDStream<ExtendedRecord> windowDStream = accessLogDStream.window(
                WINDOW_LENGTH, SLIDE_INTERVAL);

        windowDStream.foreachRDD((VoidFunction<JavaRDD<ExtendedRecord>>) rdd_records1 -> {
            if (rdd_records1.count() == 0) {
                System.out.println("No access logs in this time interval");
                return;
            }

            */
/*System.out.println("LOGISTIC REGRESSION:");

         JavaRDD<Tuple2<Integer, Double>> valuesAndPred = rdd_records1
                    .map(s -> new Tuple2<>(s.getTripDuration(), logisticRegressionModel
                            .predict(Vectors.dense(s.getTestBool(), s.getTripDuration())))
                    );

            valuesAndPred.collect().forEach(t -> System.out.println(t._1 + " " + t._2));

            System.out.println("NAIVE BAYES:");

            valuesAndPred = rdd_records1
                    .map(s -> new Tuple2<>(s.getTestBool(), naiveBayesModel
                            .predict(Vectors.dense(s.getGender(), s.getTripDuration())))
                    );

            valuesAndPred.collect().forEach(t -> System.out.println(t._1 + " " + t._2));*//*


            JavaRDD<Tuple2<Integer, Double>> valuesAndPred = rdd_records1
                    .map(point -> new Tuple2<>(point.getTripDuration(), model
                            .predict(Vectors.dense(point.getTest()))
                    ));

            valuesAndPred.collect().forEach(t -> System.out.println(t._1 + " " + t._2));

            return;
        });

        // Start the streaming server.
        jssc.start(); // Start the computation
        jssc.awaitTermination(); // Wait for the computation to terminate
    }
}
*/
