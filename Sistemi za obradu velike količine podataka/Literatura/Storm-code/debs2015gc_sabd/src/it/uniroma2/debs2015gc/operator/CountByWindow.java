package it.uniroma2.debs2015gc.operator;

import it.uniroma2.debs2015gc.utils.Window;
import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.*;

public class CountByWindow extends BaseRichBolt {

	public static final String S_PULSE 				= "sPulse";
	public static final String F_MSGID				= "MSGID";
	public static final String F_PICKUP_DATATIME	= "pickupDatatime";
	public static final String F_DROPOFF_DATATIME	= "dropoffDatatime";
	public static final String F_ROUTE 				= "route";
	public static final String F_COUNT 				= "count";
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
     */

	private static final int  WINDOW_SIZE 		= 30; 
	private static final double MIN_IN_MS 		= 60 * 1000; 
	private static final long serialVersionUID = 1L;	
	private OutputCollector collector;

	private long latestCompletedTimeframe;
	
	Map<String, Window> windowPerRoute;
	
	public CountByWindow(){

	}
	
    @Override
    public void prepare(@SuppressWarnings("rawtypes") Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        this.collector=outputCollector; 
        this.latestCompletedTimeframe = 0;
        this.windowPerRoute = new HashMap<String, Window>();
        
    }
    
    @Override
	public void execute(Tuple tuple) {
    	
    	if (tuple.getSourceStreamId().equals(Metronome.S_METRONOME)){
    		
    		handleMetronomeMessage(tuple);  //sliding window based on event time
    		
    	} else {
    		
    		handleTaxiReport(tuple);
    		
    	}
    
    }
    
    private void handleMetronomeMessage(Tuple tuple){
    
		String msgId 			= tuple.getStringByField(Metronome.F_MSGID);
		String time		 		= tuple.getStringByField(Metronome.F_TIME);
		String pickupDatatime	= tuple.getStringByField(Metronome.F_PICKUP_DATATIME);
		String dropoffDatatime 	= tuple.getStringByField(Metronome.F_DROPOFF_DATATIME);
		String timestamp 		= tuple.getStringByField(Metronome.F_TIMESTAMP);

		long latestTimeframe = roundToCompletedMinute(time);
		
		if (this.latestCompletedTimeframe < latestTimeframe){
			
			int elapsedMinutes = (int) Math.ceil((latestTimeframe - latestCompletedTimeframe) / (MIN_IN_MS));
			List<String> expiredRoutes = new ArrayList<String>();
			
			for (String r : windowPerRoute.keySet()){
				
				Window w = windowPerRoute.get(r);
				if (w == null){
					continue;
				}
				
				w.moveForward(elapsedMinutes);
				String rCount = String.valueOf(w.getEstimatedTotal());
				
				/* Reduce memory by removing windows with no data */
				if (w.getEstimatedTotal() == 0)
					expiredRoutes.add(r);

				Values v = new Values();
				v.add(msgId);
				v.add(pickupDatatime); 	
				v.add(dropoffDatatime); 	 
				v.add(r);	 
				v.add(rCount);  	
				v.add(timestamp);
				collector.emit(v);
			}
			
			/* Reduce memory by removing windows with no data */
			for (String r : expiredRoutes){
				windowPerRoute.remove(r);
			}
			
			this.latestCompletedTimeframe = latestTimeframe;
			
		} 
		
		collector.ack(tuple);			

    }
    
    private void handleTaxiReport(Tuple tuple){
    	
		String msgId 			= tuple.getStringByField(ComputeCellID.F_MSGID);
		String route 			= tuple.getStringByField(ComputeCellID.F_ROUTE);
		String dropoffDataTime	= tuple.getStringByField(ComputeCellID.F_DROPOFF_DATATIME);
		String pickupDatatime	= tuple.getStringByField(ComputeCellID.F_PICKUP_DATATIME);
		String time 			= tuple.getStringByField(ComputeCellID.F_TIME);
		String timestamp 		= tuple.getStringByField(ComputeCellID.F_TIMESTAMP);

		long latestTimeframe = roundToCompletedMinute(time);
		
		if (this.latestCompletedTimeframe < latestTimeframe){
			
			int elapsedMinutes = (int) Math.ceil((latestTimeframe - latestCompletedTimeframe) / (MIN_IN_MS));
			List<String> expiredRoutes = new ArrayList<String>();
			
			for (String r : windowPerRoute.keySet()){
				
				Window w = windowPerRoute.get(r);
				if (w == null){
					continue;
				}
				
				w.moveForward(elapsedMinutes);
				String rCount = String.valueOf(w.getEstimatedTotal());
				
				/* Emit the count of the current route after the update*/
				if (r.equals(route))
					continue;

				/* Reduce memory by removing windows with no data */
				if (w.getEstimatedTotal() == 0)
					expiredRoutes.add(r);

				Values v = new Values();
				v.add(msgId);
				v.add(pickupDatatime); 	
				v.add(dropoffDataTime); 	 
				v.add(r);	 
				v.add(rCount);
				v.add(timestamp);
				
				collector.emit(v);
				
			}

			/* Reduce memory by removing windows with no data */
			for (String r : expiredRoutes){
				windowPerRoute.remove(r);
			}
			
			this.latestCompletedTimeframe = latestTimeframe;
			
		} 
			
		/* Time has not moved forward. Update and emit count */
		Window w = windowPerRoute.get(route);
		if (w == null){
			w = new Window(WINDOW_SIZE);
			windowPerRoute.put(route, w);
		}
		
		w.increment();
		
		/* Retrieve route frequency in the last 30 mins */
		String count = String.valueOf(w.getEstimatedTotal());

		Values values = new Values();
		values.add(msgId);
		values.add(pickupDatatime); 	
		values.add(dropoffDataTime); 	 
		values.add(route);	 
		values.add(count);  	
		values.add(timestamp);
		
		collector.emit(values);
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
    	
        outputFieldsDeclarer.declare(new Fields(F_MSGID, F_PICKUP_DATATIME, F_DROPOFF_DATATIME, F_ROUTE, F_COUNT, F_TIMESTAMP));
    
    }

}