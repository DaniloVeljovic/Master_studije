package it.uniroma2.debs2015gc.operator;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

public class Metronome extends BaseRichBolt {

	public static final String S_METRONOME 			= "sMetronome";
	public static final String F_TIME				= "time";
	public static final String F_PICKUP_DATATIME 	= "pickupDatatime";
	public static final String F_DROPOFF_DATATIME 	= "dropoffDatatime";
	public static final String F_MSGID				= "msgId";
	public static final String F_TIMESTAMP 			= "timestamp";
	private static final long serialVersionUID = 1L;	
	private OutputCollector collector;

	private long currentTime = 0; 
	private long latestMsgId = 0;
	
	public Metronome(){

	}
	
    @Override
    public void prepare(@SuppressWarnings("rawtypes") Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        this.collector=outputCollector; 

    }
    
    @Override
	public void execute(Tuple tuple) {
    	
    	/* Emit message every (simulated) minute */
		String tMsgId 		= tuple.getStringByField(FilterByCoordinates.F_MSGID);
		String dTime 		= tuple.getStringByField(FilterByCoordinates.F_DROPOFF_TIMESTAMP);
		String pDatatime 	= tuple.getStringByField(FilterByCoordinates.F_PICKUP_DATATIME);
		String dDatatime 	= tuple.getStringByField(FilterByCoordinates.F_PICKUP_DATATIME);
		String timestamp	= tuple.getStringByField(FilterByCoordinates.F_TIMESTAMP);
	
		long msgId = Long.valueOf(tMsgId);
		long time = roundToCompletedMinute(dTime);
		
		if (this.latestMsgId < msgId && 
				this.currentTime < time){
	
			this.latestMsgId = msgId;
			this.currentTime = time;
			
			Values values = new Values();
			values.add(tMsgId);
			values.add(String.valueOf(time));
			values.add(pDatatime);
			values.add(dDatatime);
			values.add(timestamp);
			collector.emit(S_METRONOME, values);   // To observe: event time
		
		} else {
			/* time did not go forward */
			
		}
		
		collector.ack(tuple);			

	}
    

	private long roundToCompletedMinute(String timestamp) {

		Date d = new Date(Long.valueOf(timestamp));
		Calendar date = new GregorianCalendar();
		date.setTime(d);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		return date.getTime().getTime();
	
	}

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    	
        outputFieldsDeclarer.declareStream(S_METRONOME, new Fields(F_MSGID, F_TIME, F_PICKUP_DATATIME, F_DROPOFF_DATATIME, F_TIMESTAMP));
        
    }

}