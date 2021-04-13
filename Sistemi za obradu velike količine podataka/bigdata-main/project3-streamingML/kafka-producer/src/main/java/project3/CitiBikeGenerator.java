package project3;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CitiBikeGenerator {

    public static final String HADOOP_SERVER = System.getenv("ES_HADOOP_SERVER");
    public static final String KAFKA_SERVER = System.getenv("ES_KAFKA_SERVER");
    public static final String DATA_FILE = System.getenv("ES_FILE_PATH");
    public static final String KAFKA_PORT = System.getenv("ES_KAFKA_PORT");


    public static CitiBikeGenerator getInstance(){
        return new CitiBikeGenerator();
    }

    public void generateMessages(String group, String topic, Long millisToSleep) throws IOException, InterruptedException, URISyntaxException {
        //Can be parametrized or even made a static method

        KafkaProducer<String, String> kafkaProducer = createKafkaProducer();

        FileSystem hdfs = FileSystem.get(new URI(HADOOP_SERVER), new Configuration());
        Path path = new Path(DATA_FILE);
        FSDataInputStream stream = hdfs.open(path);

        BufferedReader br = new BufferedReader(new InputStreamReader(stream.getWrappedStream()));

        String line = br.readLine();

        while(line != null){

            ProducerRecord<String, String> rec = new ProducerRecord<>(topic,  line);

            kafkaProducer.send(rec);

            System.out.println("[PRODUCER] Sent message: " + line);
            //LOGGER.debug("[PRODUCER] Sent message: " + line);
            line = br.readLine();

            Thread.sleep(millisToSleep);
        }
    }

    public static KafkaProducer<String, String> createKafkaProducer(){
        String BOOTSTRAP_SERVER = KAFKA_SERVER + ":" + KAFKA_PORT;

        Properties properties = new Properties();

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "1");

        return new KafkaProducer<String, String>(properties);
    }

    private static KafkaConsumer<String, String> createConsumer() {
        String BOOTSTRAP_SERVER = KAFKA_SERVER + ":" + KAFKA_PORT;
        final Properties props = new Properties();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                BOOTSTRAP_SERVER);
        props.put(ConsumerConfig.GROUP_ID_CONFIG,
                "KafkaExampleConsumer");
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class.getName());

        // Create the consumer using props.
        final KafkaConsumer<String, String> consumer =
                new KafkaConsumer<>(props);

        // Subscribe to the topic.
        consumer.subscribe(Collections.singletonList("start_streaming"));
        return consumer;
    }


    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException, URISyntaxException {
        String BOOTSTRAP_SERVER = KAFKA_SERVER + ":" + KAFKA_PORT;

        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        AdminClient kafkaAdminClient = KafkaAdminClient.create(properties);
        ListTopicsResult listTopicsResult = kafkaAdminClient.listTopics();
        if (!listTopicsResult.names().get().contains("topic")) {
            CreateTopicsResult result = kafkaAdminClient.createTopics(
                    Stream.of("topic").map(
                            name -> new NewTopic(name, 3, (short) 1)
                    ).collect(Collectors.toList())
            );
            System.out.println("[PRODUCER]: Created topic 'topic'.");
        }
        if (!listTopicsResult.names().get().contains("start_streaming")) {
            CreateTopicsResult result = kafkaAdminClient.createTopics(
                    Stream.of("start_streaming").map(
                            name -> new NewTopic(name, 3, (short) 1)
                    ).collect(Collectors.toList())
            );
            System.out.println("[PRODUCER]: Created topic 'start_streaming'.");
        }


        //spark-streaming-sample-group spark-streaming-sample-topic 50 5000
        //Can be hardcoded for testing purposes.
        String group = "args[0]";
        String topics = "topic";
        //Integer iterations = Integer.parseInt(args[2]);
        Long millisToSleep = 5000L; //na 5s

        CitiBikeGenerator generator = CitiBikeGenerator.getInstance();

        KafkaConsumer<String, String> consumer = createConsumer();



        while (true) {
            final ConsumerRecords<String, String> consumerRecords =
                    consumer.poll(Duration.ofMillis(5000L));

            if (consumerRecords.count()==0) {
                System.out.println("[GENERATOR]: Waiting...");
            }else {
                System.out.println("[GENERATOR]: Starting to send data");
                break;
            }
        }
        consumer.close();

        generator.generateMessages(group, topics, millisToSleep);
    }

}
