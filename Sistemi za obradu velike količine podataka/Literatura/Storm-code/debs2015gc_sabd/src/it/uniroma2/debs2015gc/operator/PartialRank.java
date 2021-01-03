package it.uniroma2.debs2015gc.operator;

import com.google.gson.Gson;
import it.uniroma2.debs2015gc.utils.RankItem;
import it.uniroma2.debs2015gc.utils.Ranking;
import it.uniroma2.debs2015gc.utils.TopKRanking;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class PartialRank extends BaseRichBolt {

	public static final String S_PULSE 				= "sPulse";
	public static final String F_MSGID				= "MSGID";
	public static final String F_PICKUP_DATATIME	= "pickupDatatime";
	public static final String F_DROPOFF_DATATIME	= "dropoffDatatime";
	public static final String F_RANKING 			= "ranking";
	public static final String F_TIMESTAMP 			= "timestamp";
	
	
	
    /* 
     * DEBS 2015 GC: 
     *
     * The goal of the query is to find the top 10 most frequent routes 
     * during the last 30 minutes. A route is represented by a starting 
     * grid cell and an ending grid cell. All routes completed within 
     * the last 30 minutes are considered for the query. The output query
     * results must be updated whenever any of the 10 most frequent 
     * routes changes.
     *
     *  
     */

	private static final int	NULL = -1;
	private static final long serialVersionUID = 1L;	
	private OutputCollector collector;
	private TopKRanking ranking;
	private int topK; 
	private Gson gson;

	public PartialRank(int topk){
		this.topK = topk;
	}
	
    @Override
    public void prepare(@SuppressWarnings("rawtypes") Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        this.collector=outputCollector; 
        this.ranking = new TopKRanking(topK);
		this.gson = new Gson();

    }
    
    @Override
	public void execute(Tuple tuple) {
    	
		String msgId 			= tuple.getStringByField(CountByWindow.F_MSGID);
		String route 			= tuple.getStringByField(CountByWindow.F_ROUTE);
		String dropoffDataTime	= tuple.getStringByField(CountByWindow.F_DROPOFF_DATATIME);
		String pickupDatatime	= tuple.getStringByField(CountByWindow.F_PICKUP_DATATIME);
		String sCount			= tuple.getStringByField(CountByWindow.F_COUNT);
		String timestamp 		= tuple.getStringByField(CountByWindow.F_TIMESTAMP);

		int count = NULL;
		try{
			count = Integer.valueOf(sCount);
		} catch (NumberFormatException nfe) {
 			collector.ack(tuple);			
			return;			
		}
		
		/* Update local rank */
		long rts = Long.valueOf(timestamp); 
		RankItem item = new RankItem(route, count, rts);
		boolean updated = ranking.update(item);
		
		/* Emit if the local top10 is changed */
		if (updated){
			Ranking topK = ranking.getTopK();
			String serializedRanking = gson.toJson(topK);

			Values values = new Values();
			values.add(msgId);
			values.add(pickupDatatime); 	
			values.add(dropoffDataTime); 	 
			values.add(serializedRanking);	
			values.add(timestamp);
			
			collector.emit(values);
			
		}

		collector.ack(tuple);			

    }
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    	
        outputFieldsDeclarer.declare(new Fields(F_MSGID, F_PICKUP_DATATIME, F_DROPOFF_DATATIME, F_RANKING, F_TIMESTAMP));
    
    }

}