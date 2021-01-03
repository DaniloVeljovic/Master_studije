package rollingcount2;

import org.apache.storm.spout.SpoutOutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichSpout;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Values;
import org.apache.storm.utils.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Random;

/*
    Random sentence source
 */

public class RandomSentenceSpout extends BaseRichSpout {

    private static final Logger LOG = LoggerFactory.getLogger(RandomSentenceSpout.class);
    private SpoutOutputCollector _collector;
    private Random _rand;

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {
        _collector = collector;
        _rand = new Random();
    }

    @Override
    public void nextTuple() {
        Utils.sleep(1000);
        String[] sentences = new String[]{
                "the cow jumped over the moon",
                "an apple a day keeps the doctor away",
                "four score and seven years ago",
                "snow white and the seven dwarfs",
                "i am at two with nature"
        };

        final String sentence = sentences[_rand.nextInt(sentences.length)];
        LOG.debug("Emitting tuple: {}", sentence);
        _collector.emit(new Values(sentence));

    }

    public void ack(Object id) { }
    public void fail(Object id) { }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

        declarer.declare(new Fields("word"));

    }

}
