package rollingcount2;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseWindowedBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;
import org.apache.storm.windowing.TupleWindow;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*  To observe: - BaseWindowedBolt instead BaseRichBolt;
                - tick mechanism managed directly by Storm 1.0 (execution time, NOT event time)
 */

public class WordCountWindowBasedBolt extends BaseWindowedBolt {

    Map<String, Integer> counts = new HashMap<String, Integer>();
    OutputCollector collector;

    WordCountWindowBasedBolt(){

    }

    @Override
    public void prepare(Map stormConf,
                        TopologyContext context,
                        OutputCollector collector) {
        this.collector = collector;
    }

    @Override
    public void execute(TupleWindow tuples) {

        List<Tuple> incoming = tuples.getNew();

        for (Tuple tuple : incoming){

            String word = tuple.getString(0);
            Integer count = counts.get(word);
            if (count == null)
                count = 0;
            count++;
            counts.put(word, count);

        }


        List<Tuple> expired = tuples.getExpired();

        for (Tuple tuple : expired){

            String word = tuple.getString(0);
            Integer count = counts.get(word);
            if (count != null){
                count--;
                if (count > 0)
                    counts.put(word, count);
                else
                    counts.remove(word);
            }

        }

        for (String word : counts.keySet()){
            Integer count = counts.get(word);
            collector.emit(new Values(word, count));
        }

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("word", "count"));
    }
}