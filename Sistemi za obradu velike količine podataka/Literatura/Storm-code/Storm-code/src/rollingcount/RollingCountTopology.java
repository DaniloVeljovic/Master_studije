package rollingcount;


import exclamation.RandomNamesSpout;
import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.tuple.Fields;

/**
 * This topology does a continuous computation of the top N words that the topology has seen in terms of cardinality.
 * The top N computation is done in a completely scalable way, and a similar approach could be used to compute things
 * like trending topics or trending images on Twitter.
 */
public class RollingCountTopology {

    private static final int DEFAULT_RUNTIME_IN_SECONDS = 60;
    private static final int TOP_N = 5;

    private static final String RABBITMQ_HOST = "rabbitmq";
    private static final String RABBITMQ_USER = "rabbitmq";
    private static final String RABBITMQ_PASS = "rabbitmq";
    private static final String RABBITMQ_QUEUE = "rollingcount";


    public static void main(String[] args) throws Exception {

        Config conf = new Config();
        conf.setDebug(false);

        TopologyBuilder builder = new TopologyBuilder();

        String spoutId = "wordGenerator";
        String counterId = "counter";
        String intermediateRankerId = "intermediateRanker";
        String totalRankerId = "finalRanker";

        builder.setSpout(spoutId, new RandomNamesSpout(), 5);

        builder.setBolt(counterId,
                new RollingCountBolt(9, 3), 4)
                .fieldsGrouping(spoutId, new Fields("word"));

        /* Two operators that realize the top-k ranking in two steps (typical design pattern):
        IntermediateRankingBolt can be distributed and parallelized,
        whereas totalRankingsBolt is centralized and computes the global ranking */

        builder.setBolt(intermediateRankerId, new IntermediateRankingBolt(TOP_N), 4)
                .fieldsGrouping(counterId, new Fields("obj"));

        builder.setBolt(totalRankerId, new TotalRankingsBolt(TOP_N),1)
                .globalGrouping(intermediateRankerId);

        builder.setBolt("exporter",
                new RollingCountExporter(RABBITMQ_HOST, RABBITMQ_USER, RABBITMQ_PASS, RABBITMQ_QUEUE ),
                1)
                .shuffleGrouping(totalRankerId);

        if (args != null && args.length > 0) {
            conf.setNumWorkers(3);

            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());

        } else {

            conf.setMaxTaskParallelism(3);
            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("word-count", conf, builder.createTopology());
            Thread.sleep(DEFAULT_RUNTIME_IN_SECONDS * 1000);
            cluster.shutdown();

        }
    }
}

