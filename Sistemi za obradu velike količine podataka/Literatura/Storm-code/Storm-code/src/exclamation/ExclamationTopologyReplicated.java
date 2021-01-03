package exclamation;

import org.apache.storm.Config;
import org.apache.storm.LocalCluster;
import org.apache.storm.StormSubmitter;
import org.apache.storm.topology.TopologyBuilder;
import org.apache.storm.utils.Utils;

/**
 * This is a basic example of a Storm topology.
 */
public class ExclamationTopologyReplicated {

    public static void main(String[] args) throws Exception {
        TopologyBuilder builder = new TopologyBuilder();

        builder.setSpout("word", new RandomNamesSpout(), 5);

        builder.setBolt("exclaim1", new ExclamationBolt(), 3) // add first exclamation point
                .shuffleGrouping("word");

        builder.setBolt("exclaim2", new ExclamationBolt(), 2) // add second exclamation point
                .shuffleGrouping("exclaim1");

        Config conf = new Config();
        conf.setDebug(true);

        /* If we specify the name of the topology,
         *  we assume that the topology should be executed on the cluster. */
        if (args != null && args.length > 0) {

            conf.setNumWorkers(3);
            StormSubmitter.submitTopologyWithProgressBar(args[0], conf, builder.createTopology());

        } else {

            LocalCluster cluster = new LocalCluster();
            cluster.submitTopology("test", conf, builder.createTopology());
            Utils.sleep(10000);
            cluster.killTopology("test");
            cluster.shutdown();

        }
    }

}

