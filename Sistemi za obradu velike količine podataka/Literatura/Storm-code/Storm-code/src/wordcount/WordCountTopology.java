package wordcount;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

/**
 * This topology demonstrates Storm's stream groupings.
 */
public class WordCountTopology {

    private static final String RABBITMQ_HOST = "rabbitmq";
    private static final String RABBITMQ_USER = "rabbitmq";
    private static final String RABBITMQ_PASS = "rabbitmq";
    private static final String RABBITMQ_QUEUE = "wordcount";

    public static void main(String[] args) throws Exception {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new RandomSentenceSpout(), 5);

        builder.setBolt("split", new SplitSentenceBolt(), 8)
                .shuffleGrouping("spout");

        builder.setBolt("count", new WordCountBolt(), 12)
                .fieldsGrouping("split", new Fields("word"));

        Config conf = new Config();
        conf.setDebug(true);

        if (args != null && args.length > 0) {

            builder.setBolt("exporter",
                    new RabbitMQExporterBolt(
                            RABBITMQ_HOST, RABBITMQ_USER,
                            RABBITMQ_PASS, RABBITMQ_QUEUE ),
                    3)
               .shuffleGrouping("count");

            conf.setNumWorkers(3);

            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());

        } else {

            conf.setMaxTaskParallelism(3);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("word-count", conf, builder.createTopology());
            Thread.sleep(10000);
            cluster.shutdown();

        }
    }



}