package project2;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;

import java.io.*;
import java.util.Properties;

public class CitiBikeGenerator {

    //private static final Log LOGGER = LogFactory.getLog(CitiBikeGenerator.class);
    public static final String BOOTSTRAP_SERVER = "localhost:9092";
    private static final String DATA_FILE = "src/main/resources/201306-citibike-tripdata.csv";

    public static CitiBikeGenerator getInstance(){
        return new CitiBikeGenerator();
    }

    public void generateMessages(String group, String topic, Long millisToSleep) throws IOException, InterruptedException {
        //Can be parametrized or even made a static method
        KafkaProducer kafkaProducer = createKafkaProducer();

        File f = new File(DATA_FILE);
        FileReader fr = new FileReader(f);
        BufferedReader reader = new BufferedReader(fr);
        String line = reader.readLine();

          while(line != null){

            ProducerRecord<String, String> rec = new ProducerRecord<>(topic,  line);

            kafkaProducer.send(rec);

            System.out.println("[PRODUCER] Sent message: " + line);
            //LOGGER.debug("[PRODUCER] Sent message: " + line);
            line = reader.readLine();

            Thread.sleep(millisToSleep);
        }
    }

    public KafkaProducer createKafkaProducer(){

        Properties properties = new Properties();

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.ACKS_CONFIG, "1");

        KafkaProducer<String, String> producer = new KafkaProducer<String, String>(properties);
        return producer;
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        if(args.length == 0){
            System.err.println("Wrong number of arguments");
            System.exit(-1);
        }

        //Can be hardcoded for testing purposes.
        String group = args[0];
        String topics = args[1];
        Integer iterations = Integer.parseInt(args[2]);
        Long millisToSleep = Long.parseLong(args[3]);

        CitiBikeGenerator generator = CitiBikeGenerator.getInstance();

        generator.generateMessages(group, topics, millisToSleep);
    }

}
