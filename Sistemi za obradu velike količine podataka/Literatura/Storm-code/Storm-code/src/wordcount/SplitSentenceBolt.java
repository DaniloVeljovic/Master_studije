package wordcount;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;
import java.util.regex.Pattern;

public class SplitSentenceBolt extends BaseRichBolt {

    private OutputCollector _collector;
    private Pattern SPACE;

    public SplitSentenceBolt() {

    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {

        this._collector = collector;
        this.SPACE = Pattern.compile(" ");

    }

    @Override
    public void execute(Tuple input) {

        String sentence = input.getString(0);

        for (String word : SPACE.split(sentence)){

            _collector.emit(new Values(word));

        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {

        declarer.declare(new Fields("word"));

    }

    @Override
    public Map<String, Object> getComponentConfiguration() {

        return null;

    }

}