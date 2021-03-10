import org.apache.spark.ml.classification.RandomForestClassifier
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator
import org.apache.spark.ml.feature.VectorAssembler
import org.apache.spark.sql.{Dataset, Row, SparkSession}

object Application {
  def main(args: Array[String]): Unit = {

    // Get environment variables for spark and hadoop data source
    val sparkMaster = sys.env("ES_SPARK_MASTER")
    val dataSource = sys.env("ES_DATA_SOURCE")
    val trainedModelTarget = sys.env("ES_TRAINED_MODEL_TARGET")

    val featureLabel = "label:SLEEPING"

    // Connect to spark master
    val spark = SparkSession.builder
      .appName("[Project3] Spark Machine Learning")
      .master(sparkMaster)
      .getOrCreate()

    // Read from data source
    val df = spark.read.option("header", "true").option("inferSchema", "true").csv(dataSource)
    df.printSchema()
    val filteredDf = df.filter(df.col(featureLabel).isNotNull)

    // Add features aggregate column using vector assembler
    val assembler = new VectorAssembler().setInputCols(filteredDf.schema.names)
      .setOutputCol("features")
      .setHandleInvalid("keep")
    val transformedData = assembler.transform(filteredDf)

    val Array(trainingData: Dataset[Row], testData: Dataset[Row]) = transformedData.randomSplit(Array(0.7, 0.3), 1337)

    // Create the classifier and set its parameters
    val randomForestClassifier = new RandomForestClassifier()
      .setLabelCol(featureLabel)
      .setFeaturesCol("features")
      .setPredictionCol("prediction")

    // Train the model
    val model = randomForestClassifier.fit(trainingData)
    model.write.overwrite.save(trainedModelTarget)

    val predictions = model.transform(testData)

    // Evaluate machine learning classifier
    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol(featureLabel)
      .setPredictionCol("prediction")
      .setMetricName("accuracy")
    val accuracy = evaluator.evaluate(predictions)
    println(s"Test accuracy = ${accuracy}")

    spark.stop()
    spark.close()
  }
}
