import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.streaming.dstream.InputDStream
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe
import org.apache.spark.streaming.kafka010._
import org.apache.spark.ml.classification.RandomForestClassificationModel
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.types.DataTypes.createStructField
import org.apache.spark.sql.types.{DataTypes, StructType}
import org.apache.spark.sql.{RowFactory, SparkSession}

object Application {
  def main(args: Array[String]): Unit = {
    val sparkMaster = sys.env("ES_SPARK_MASTER")
    val kafkaServer = sys.env("ES_KAFKA_SERVER")
    val kafkaPort = sys.env("ES_KAFKA_PORT")
    val trainedModelPath = sys.env("ES_TRAINED_MODEL_PATH")

    val windowDuration = Seconds(10)
    val topic = "project3-extrasensory"

    val model = RandomForestClassificationModel.load(trainedModelPath).setFeaturesCol("features").setPredictionCol("prediction")

    val spark = SparkSession.builder()
      .appName("[Project3] ML Streaming")
      .master(sparkMaster)
      .getOrCreate()

    val mapper = new ExtraSensoryCsvMapper()

    val streamingContext = new StreamingContext(spark.sparkContext, windowDuration)
    val stream = getSparkStream(streamingContext, kafkaServer, kafkaPort, Array(topic))
    val rows = stream
      .map(rdd => mapper.map(rdd.value()))
      .filter(rdd => rdd != null)
      .map(item => RowFactory.create(mapper.getSchema().map(s => if (item(s) == "") null.asInstanceOf[Object] else item(s).toDouble.asInstanceOf[Object]):_*))

    val schema: StructType = DataTypes.createStructType(mapper.getSchema().map(s => createStructField(s, DataTypes.DoubleType, true)))

    rows.foreachRDD(rdd => {
      val vectorAssembler: VectorAssembler = new VectorAssembler()
        .setInputCols(mapper.getSchema())
        .setOutputCol("features")
        .setHandleInvalid("keep")
      val dataset = spark.createDataFrame(rdd, schema)
      val transformed = vectorAssembler.transform(dataset)
      val predictions = model.transform(transformed)
      predictions.show()
    })

    streamingContext.start()
    streamingContext.awaitTermination()
  }

  def getSparkStream(ssc: StreamingContext, kafkaServer: String, kafkaPort: String, topics: Array[String]): InputDStream[ConsumerRecord[String, String]] = {
    val kafkaParams = Map[String, Object](
      "bootstrap.servers" -> s"$kafkaServer:$kafkaPort",
      "key.deserializer" -> classOf[StringDeserializer],
      "value.deserializer" -> classOf[StringDeserializer],
      "group.id" -> "project3",
      "auto.offset.reset" -> "latest",
      "enable.auto.commit" -> (false: java.lang.Boolean)
    )

    KafkaUtils.createDirectStream[String, String](
      ssc,
      PreferConsistent,
      Subscribe[String, String](topics, kafkaParams)
    )
  }
}