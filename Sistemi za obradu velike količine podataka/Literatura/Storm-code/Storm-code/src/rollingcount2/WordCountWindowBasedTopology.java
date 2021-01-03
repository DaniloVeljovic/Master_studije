package rollingcount2;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;

/**
 * Storm 1.0 has explicitly introduced the concept of window.
 */
public class WordCountWindowBasedTopology {

    private static final String RABBITMQ_HOST = "rabbitmq";
    private static final String RABBITMQ_USER = "rabbitmq";
    private static final String RABBITMQ_PASS = "rabbitmq";
    private static final String RABBITMQ_QUEUE = "wordcount-window";

    public static void main(String[] args) throws Exception {

        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("spout", new RandomSentenceSpout(), 5);

        builder.setBolt("split", new SplitSentenceBolt(), 8)
                .shuffleGrouping("spout");

        builder.setBolt("count", new WordCountWindowBasedBolt()
//                .withWindow(BaseWindowedBolt.Count.of(10))
                .withWindow(
                        BaseWindowedBolt.Duration.seconds(9),
                        BaseWindowedBolt.Duration.seconds(3)
                ),
                12)
                .fieldsGrouping("split", new Fields("word"));

        Config conf = new Config();
        conf.setDebug(true);

        if (args != null && args.length > 0) {

            builder.setBolt("exporter",
                    new RabbitMQExporterBolt(RABBITMQ_HOST, RABBITMQ_USER, RABBITMQ_PASS, RABBITMQ_QUEUE ),
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