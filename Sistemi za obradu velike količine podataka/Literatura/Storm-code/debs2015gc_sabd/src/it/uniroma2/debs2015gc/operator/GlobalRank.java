package it.uniroma2.debs2015gc.operator;

import com.google.gson.Gson;
import it.uniroma2.debs2015gc.utils.RabbitMQManager;
import it.uniroma2.debs2015gc.utils.RankItem;
import it.uniroma2.debs2015gc.utils.Ranking;
import it.uniroma2.debs2015gc.utils.TopKRanking;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;

import java.util.List;
import java.util.Map;

public class GlobalRank extends BaseRichBolt {

	public static final String S_PULSE 				= "sPulse";
	public static final String F_MSGID				= "MSGID";
	public static final String F_PICKUP_DATATIME	= "pickupDatatime";
	public static final String F_DROPOFF_DATATIME	= "dropoffDatatime";
	public static final String F_OUTPUT				= "output";
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

	private static final long serialVersionUID = 1L;	
	private OutputCollector collector;
	private TopKRanking ranking;
	private int topK; 
	private Gson gson;
	private RabbitMQManager rabbitmq;

	private boolean USE_RABBIT;
	private String rabbitMqHost;
	private String rabbitMqUsername;
	private String rabbitMqPassword;

	
	
    public GlobalRank(int topK, String rabbitMqHost, String rabbitMqUsername, String rabbitMqPassword) {
		super();
		this.topK = topK;
		this.rabbitMqHost = rabbitMqHost;
		this.rabbitMqUsername = rabbitMqUsername;
		this.rabbitMqPassword = rabbitMqPassword;
		this.USE_RABBIT = true;
	}

    
	public GlobalRank(int topK) {
		super();
		this.topK = topK;
		this.USE_RABBIT = false;
	}


	@Override
    public void prepare(@SuppressWarnings("rawtypes") Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        this.collector=outputCollector; 
        this.ranking = new TopKRanking(topK);
		this.gson = new Gson();
		
		if (USE_RABBIT){
			this.rabbitmq = new RabbitMQManager(rabbitMqHost, rabbitMqUsername, rabbitMqPassword);
		}
		
    }
    
    @Override
	public void execute(Tuple tuple) {
    	
    	/* TODO: change the logic of this operator. 
    	 * 
    	 * A new message should be emitted every (simulated) minute, 
    	 * if an update is observed. This current implementation does
    	 * not explicitly take into account the passing of 
    	 * time.
    	 *  
    	 */
    	
		String serializedRanking= tuple.getStringByField(PartialRank.F_RANKING);
		String dropoffDataTime	= tuple.getStringByField(PartialRank.F_DROPOFF_DATATIME);
		String pickupDatatime	= tuple.getStringByField(PartialRank.F_PICKUP_DATATIME);
		String timestamp 		= tuple.getStringByField(PartialRank.F_TIMESTAMP);

		Ranking partialRanking = gson.fromJson(serializedRanking, Ranking.class);

		/* Update global rank */
		boolean updated = false; 
		for (RankItem item : partialRanking.getRanking()){
			updated |= ranking.update(item);
		}
				
		/* Emit if the local top10 is changed */
		if (updated){

			long delay = 0;
			try{
				delay = System.currentTimeMillis() - Long.valueOf(timestamp);
			} catch (NumberFormatException nfe){ }

			List<RankItem> globalTopK = ranking.getTopK().getRanking();
			String output = pickupDatatime + ", " + dropoffDataTime+ ", ";

			for (int i = 0; i < globalTopK.size(); i++){
				RankItem item = globalTopK.get(i);
				output += item.getRoute();
				output += ", ";
			}
			
			if (globalTopK.size() < topK){
				int i = topK - globalTopK.size(); 
				for (int j = 0; j < i; j++){
					output += "NULL";
					output += ", ";
				}
			}

			/* piggyback delay */
			output += String.valueOf(delay);
			
			if (USE_RABBIT)
				rabbitmq.send(output);
			
			System.out.println(">> " + output);
	         
		}

		collector.ack(tuple);			

    }
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    	
        outputFieldsDeclarer.declare(new Fields(F_MSGID, F_PICKUP_DATATIME, F_DROPOFF_DATATIME, F_OUTPUT));
    
    }

}