import java.io.{BufferedReader, InputStreamReader}
import java.net.URI
import java.util.Properties

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.{FileSystem, Path}
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object Application {

  def main(args: Array[String]): Unit = {
    val hadoopServer = sys.env("ES_HADOOP_SERVER")
    val filePath = sys.env("ES_FILE_PATH")
    val kafkaServer = sys.env("ES_KAFKA_SERVER")
    val kafkaPort = sys.env("ES_KAFKA_PORT")
    val delayBetweenSending = sys.env("ES_MILLISECONDS_BETWEEN_MESSAGES").toInt

    val serializer = "org.apache.kafka.common.serialization.StringSerializer"
    val topic = "project3-extrasensory"
    val producer = configureProducer(s"$kafkaServer:$kafkaPort", serializer)

    val hdfs = FileSystem.get(new URI(hadoopServer), new Configuration())
    val path = new Path(filePath)
    val stream = hdfs.open(path)

    val br = new BufferedReader(new InputStreamReader(stream.getWrappedStream));
    try {
      var line: String = null
      line = br.readLine()
      while (line != null) {
        producer.send(new ProducerRecord[String, String](topic, line))
        line = br.readLine
        Thread.sleep(delayBetweenSending)
      }
    } finally {
      stream.close()
      br.close()
    }

    producer.close()
    println("All done.")
    sys.exit(0)
  }

  def configureProducer(kafkaServer: String, serializer: String): KafkaProducer[String, String] = {
    val props = new Properties()
    props.put("bootstrap.servers", kafkaServer)
    props.put("key.serializer", serializer)
    props.put("value.serializer", serializer)
    new KafkaProducer[String, String](props)
  }
}
