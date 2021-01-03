package exclamation;

import org.apache.storm.Config;
import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/* Random names source */

public class RandomNamesSpout extends BaseRichSpout {

    private final String[] words =
            new String[]{"nathan", "mike", "jackson", "golda", "bertels"};
    private boolean _isDistributed;
    private SpoutOutputCollector _collector;
    private Random rand;

    public RandomNamesSpout() {

        _isDistributed = true;

    }

    public void open(
            Map conf, TopologyContext context,
            SpoutOutputCollector collector) {
        rand = new Random();
        _collector = collector;
    }

    public void close() {

    }

    public void nextTuple() {

        Utils.sleep(1000);
        final String word = words[rand.nextInt(words.length)];
        _collector.emit(new Values(word));

    }

    public void declareOutputFields(OutputFieldsDeclarer declarer) {

        declarer.declare(new Fields("word"));

    }

    public void ack(Object msgId) {
    }

    public void fail(Object msgId) {
    }

    @Override
    public Map<String, Object> getComponentConfiguration() {
        if (!_isDistributed) {
            Map<String, Object> ret = new HashMap<String, Object>();
            ret.put(Config.TOPOLOGY_MAX_TASK_PARALLELISM, 1);
            return ret;
        } else {
            return null;
        }
    }
}
