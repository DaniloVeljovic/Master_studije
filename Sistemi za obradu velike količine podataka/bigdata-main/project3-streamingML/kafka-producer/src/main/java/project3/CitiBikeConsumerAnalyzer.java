package project3;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.spark.SparkConf;

import org.apache.spark.SparkContext;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.ForeachFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;


import org.apache.spark.api.java.function.VoidFunction;

import org.apache.spark.ml.feature.LabeledPoint;
import org.apache.spark.ml.feature.VectorAssembler;
import org.apache.spark.ml.regression.LinearRegression;
import org.apache.spark.ml.regression.LinearRegressionModel;
import org.apache.spark.ml.linalg.Vectors;
import org.apache.spark.ml.regression.LinearRegressionTrainingSummary;



import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaInputDStream;

import org.apache.spark.streaming.api.java.JavaStreamingContext;

import org.apache.spark.streaming.kafka010.ConsumerStrategies;
import org.apache.spark.streaming.kafka010.KafkaUtils;
import org.apache.spark.streaming.kafka010.LocationStrategies;
import project1.Record;
import scala.Tuple2;
import tweet.Parse;

import java.io.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CitiBikeConsumerAnalyzer {
    private static Function2<Integer, Integer, Integer> SUM_REDUCER = (a, b) -> a + b;

    private static final Log LOGGER = LogFactory.getLog(CitiBikeConsumerAnalyzer.class);

    // Stats will be computed for the last window length of time.
    private static final Duration WINDOW_LENGTH = new Duration (10000);

    // Stats will be computed every slide interval time.
    private static final Duration SLIDE_INTERVAL = new Duration(10000);

    private static final Integer N = 1;

    public static final String HADOOP_SERVER = System.getenv("ES_HADOOP_SERVER");
    public static final String KAFKA_SERVER = System.getenv("ES_KAFKA_SERVER");
    private static final String DATA_FILE = System.getenv("ES_FILE_PATH");
    private static final String KAFKA_PORT = System.getenv("ES_KAFKA_PORT");
    private static final String KAFKA_CFG_ZOOKEEPER_CONNECT = System.getenv("KAFKA_CFG_ZOOKEEPER_CONNECT");


    public static void main(String[] args) throws IOException, InterruptedException, URISyntaxException, ExecutionException {
        // Set application name
        String appName = "Spark Streaming MLib";

        String sparkMaster = System.getenv("ES_SPARK_MASTER");

        // Create a Spark Context.
        SparkConf conf = new SparkConf()
                .setAppName(appName)
                .setMaster(sparkMaster)
                .set("spark.executor.memory", "1g");
        JavaSparkContext sc = new JavaSparkContext(conf);



        // This sets the update window to be every 10 seconds.
        JavaStreamingContext jssc = new JavaStreamingContext(sc, SLIDE_INTERVAL);

        SparkSession orCreate = SparkSession.builder()
                .master(sparkMaster)
                .appName("SparkByExamples.com")
                .getOrCreate();

        System.out.println("!!!!!!!!!!CREATED SPARK SESSION!!!!!!!!!!!!!!!!!!");
/*
        Dataset<Row> training = orCreate.read().format("libsvm")
                .load(DATA_FILE);*/



        String zkQuorum = KAFKA_CFG_ZOOKEEPER_CONNECT + ":2181";
        String group = "spark-streaming-sample-groupid";
        String strTopics = "topic";
        int numThreads = 2;

        Map<String, Object> kafkaParams = new HashMap<>();
        kafkaParams.put("bootstrap.servers", KAFKA_SERVER + ":9092");
        kafkaParams.put("key.deserializer", StringDeserializer.class);
        kafkaParams.put("value.deserializer", StringDeserializer.class);
        kafkaParams.put("group.id", "groupid");
        kafkaParams.put("auto.offset.reset", "latest");
        kafkaParams.put("enable.auto.commit", false);

        Map<String, Integer> topicMap = new HashMap<String, Integer>();
        String[] topics = strTopics.split(",");
        for (String topic : topics) {
            topicMap.put(topic, numThreads);
        }

        Collection<String> topics1 = Collections.singletonList("topic");


        JavaInputDStream<ConsumerRecord<String, String>> logDataDStream = KafkaUtils.createDirectStream(jssc, LocationStrategies.PreferConsistent(), ConsumerStrategies.<String, String>Subscribe(topics1, kafkaParams));
        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("Received DStream connecting to zookeeper " + zkQuorum + " group " + group + " topics" +
                topicMap);
        System.out.println("logDataDStream: " + logDataDStream);

        JavaDStream<ExtendedRecord> accessLogDStream = logDataDStream.map(
                (a) -> {
                    String strLogMsg = a.value();
                    return Parse.parseStringToExtendedRecord(strLogMsg);
                }
        );
        accessLogDStream.print();

        FileSystem hdfs = FileSystem.get(new URI(HADOOP_SERVER), new Configuration());
        Path path = new Path(DATA_FILE);
        FSDataInputStream stream = hdfs.open(path);
        System.out.println("CONNECTED TO HADOOP, PATH " + path.toString());
        BufferedReader br = new BufferedReader(new InputStreamReader(stream.getWrappedStream()));

        String line = br.readLine();

        Dataset<Row> df = orCreate.read().option("header", "true").option("inferSchema", "true").csv("hdfs://hadoop:9000"+path.toString());
        df.printSchema();

        System.out.println("!!!!!!!!!!!!!!!!!!!!!PRINTED THE SCHEMA!!!!!!!!!!!!!!!!!");

        JavaRDD<ExtendedRecord> rdd = df.toJavaRDD().map(t ->{
            System.out.println("BEFORE PARSING");
            System.out.println(t.toString());
            ExtendedRecord extendedRecord = Parse.parseStringToExtendedRecord(t.toString());
            System.out.println("after parsing");
            System.out.println(extendedRecord);
            return extendedRecord;

        });

        System.out.println("!!!!!!!!!!!!!!!!!!!!!PARSED THE DATA!!!!!!!!!!!!!!!!!");

        //List<ExtendedRecord> collect1 = rdd.collect();

        //System.out.println("FINISHED COLLECT");

        //ArrayList<ExtendedRecord> collect = new ArrayList<>(collect1);

        //training the model
        //JavaRDD<ExtendedRecord> rdd_records = sc.textFile(DATA_FILE).map(Parse::parseStringToExtendedRecord);

        System.out.println("!!!!!!!!!!!!!!!!!!!!!BEFORE ML!!!!!!!!!!!!!!!!!");

        /*if(collect.isEmpty()){
            System.out.println("!!!!!!!!COLLECT IS EMPTY !!!!!!!!!!!!!");
        }*/

        String[] cols = {"trip_duration"};
        VectorAssembler vectorAssembler = new VectorAssembler().setInputCols(cols)
                .setOutputCol("features")
                .setHandleInvalid("keep");

        Dataset<Row> transform = vectorAssembler.transform(df);
        //transform = transform.select("features","test");

        //assert collect != null;
        //System.out.println(collect.get(0));
        //df.map(s -> new LabeledPoint(s.getTripDuration(), Vectors.dense(s.getTest()))).collect(Collectors.toList()); //regression
        //LinearRegressionModel model = new  LinearRegressionWithSGD(0.00000001, 100, 0.00000001, ); //regression
        LinearRegression lr = new LinearRegression()
                .setMaxIter(100)
                .setRegParam(0.00000001)
                .setElasticNetParam(0.8)
                .setFeaturesCol("features")
                .setLabelCol("test");

        //Dataset<Row> fromParsedData = orCreate.createDataFrame(parsedData, LabeledPoint.class);

        //fromParsedData.foreach((ForeachFunction<Row>) System.out::println);

        LinearRegressionModel model = lr.fit(transform);

        System.out.println("!!!!!!!!!!!!!!!!!!!!!AFTER ML!!!!!!!!!!!!!!!!!");

        LinearRegressionTrainingSummary trainingSummary = model.summary();
        System.out.println("numIterations: " + trainingSummary.totalIterations());
        System.out.println("objectiveHistory: " + Vectors.dense(trainingSummary.objectiveHistory()));
        trainingSummary.residuals().show();
        System.out.println("RMSE: " + trainingSummary.rootMeanSquaredError());
        System.out.println("r2: " + trainingSummary.r2());

        String BOOTSTRAP_SERVER = KAFKA_SERVER + ":" + KAFKA_PORT;
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        AdminClient kafkaAdminClient = KafkaAdminClient.create(properties);
        ListTopicsResult listTopicsResult = kafkaAdminClient.listTopics();
        if (!listTopicsResult.names().get().contains("start_streaming")) {
            CreateTopicsResult result = kafkaAdminClient.createTopics(
                    Stream.of("start_streaming").map(
                            name -> new NewTopic(name, 3, (short) 1)
                    ).collect(Collectors.toList())
            );
            System.out.println("[CONSUMER]: Created topic 'start_streaming'.");
        }

        KafkaProducer<String, String> kafkaProducer = CitiBikeGenerator.createKafkaProducer();
        ProducerRecord<String, String> rec = new ProducerRecord<>("start_streaming",  "start");
        kafkaProducer.send(rec);

        /*JavaRDD<LabeledPoint> parsedData = rdd_records.map(s -> new LabeledPoint(s.getTestBool(), Vectors.dense(s.getGender(), s.getTripDuration())));

        LogisticRegressionModel logisticRegressionModel = new LogisticRegressionWithLBFGS().setNumClasses(2).run(parsedData.rdd());

        ClassificationModel naiveBayesModel = new NaiveBayes().run(parsedData.rdd());*/

        System.out.println("[CONSUMER]: Starting the stream.");

        JavaDStream<ExtendedRecord> windowDStream = accessLogDStream.window(
                WINDOW_LENGTH, SLIDE_INTERVAL);

        windowDStream.foreachRDD((VoidFunction<JavaRDD<ExtendedRecord>>) rdd_records1 -> {
            if (rdd.count() == 0) {
                System.out.println("No access logs in this time interval");
                return;
            }

            accessLogDStream.print();
            System.out.println("[CONSUMER]: Received some data.");
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

            valuesAndPred.collect().forEach(t -> System.out.println(t._1 + " " + t._2));*/

            JavaRDD<Tuple2<Integer, Double>> valuesAndPred = rdd_records1
                    .map(point -> new Tuple2<>(point.getTripDuration(), model
                            .predict(Vectors.dense(point.getTest()))
                    ));

            valuesAndPred.collect().forEach(t -> System.out.println(t._1 + " " + t._2));

        });

        // Start the streaming server.
        jssc.start(); // Start the computation
        jssc.awaitTermination(); // Wait for the computation to terminate
    }
}
