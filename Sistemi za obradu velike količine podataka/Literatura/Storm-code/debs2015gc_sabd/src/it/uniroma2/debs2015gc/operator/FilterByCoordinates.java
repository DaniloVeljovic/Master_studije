	package it.uniroma2.debs2015gc.operator;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class FilterByCoordinates extends BaseRichBolt {

	public static final String F_MSGID				= "MSGID";
	public static final String F_PICKUP_DATATIME	= "pickupDatatime";
	public static final String F_DROPOFF_DATATIME 	= "dropoffDatatime";
	public static final String F_PICKUP_LONGITUDE 	= "pickupLongitude";
	public static final String F_PICKUP_LATITUDE	= "pickupLatitude";
	public static final String F_DROPOFF_LONGITUDE 	= "dropoffLongitude";
	public static final String F_DROPOFF_LATITUDE	= "dropoffLatitude";
	public static final String F_DROPOFF_TIMESTAMP 	= "dropoffTimestamp";
	public static final String F_TIMESTAMP 			= "timestamp";

    /* 
     * DEBS 2015 GC Data Format: 
     * 
     *   The cells for this query are squares of 500 m X 500 m. 
     *   The cell grid starts with cell 1.1, located at 41.474937, -74.913585 
     *   (in Barryville). The coordinate 41.474937, -74.913585 marks the center 
     *   of the first cell. Cell numbers increase towards the east and south, 
     *   with the shift to east being the first and the shift to south the 
     *   second component of the cell, i.e., cell 3.7 is 2 cells east and 6 
     *   cells south of cell 1.1. 
     *   
     *   The overall grid expands 150km south and 150km east from cell 1.1 with 
     *   the cell 300.300 being the last cell in the grid. All trips starting 
     *   or ending outside this area are treated as outliers and must not 
     *   be considered in the result computation.
     *   
     *   To observe:
     *   For the challenge we allow a simplified flat earth assumption for
     *   mapping coordinates to cells in the queries. You can assume that 
     *   a distance of 500 meter south corresponds to a change of 0.004491556 
     *   degrees in the coordinate system. For moving 500 meter east you can 
     *   assume a change of 0.005986 degrees in the coordinate system.
     *
     *  
     */

    private double initialLatitude 	= 41.472691222;
    private double finalLatitude 	= (41.472691222 - 300.0 * 0.004491556); 	// 40.125224422
    private double initialLongitude = -74.916578;
    private double finalLongitude 	= (-74.91657 + 300.0 * 0.005986); 			// -73.12077

    
	private static final long serialVersionUID = 1L;	
	private OutputCollector collector;
	private SimpleDateFormat sdf; 
	
	public FilterByCoordinates(){
    }

	
    @Override
    public void prepare(@SuppressWarnings("rawtypes") Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        this.collector=outputCollector; 
        this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
    }

    @Override
	public void execute(Tuple tuple) {
    	
		String msgId 	= tuple.getStringByField(RedisSpout.F_MSGID);
		String pDatatime =  tuple.getStringByField(ParseLine.F_PICKUP_DATATIME);
		String pLongitude = tuple.getStringByField(ParseLine.F_PICKUP_LONGITUDE);
		String pLatitude  = tuple.getStringByField(ParseLine.F_PICKUP_LATITUDE);
		String dDatatime =  tuple.getStringByField(ParseLine.F_DROPOFF_DATATIME);
		String dLongitude = tuple.getStringByField(ParseLine.F_DROPOFF_LONGITUDE);
		String dLatitude  = tuple.getStringByField(ParseLine.F_DROPOFF_LATITUDE);
		String timestamp  = tuple.getStringByField(ParseLine.F_TIMESTAMP);
		
		Double pLong = null;
		Double pLat = null;
		Double dLong = null;
		Double dLat = null;
		try{

			pLong = Double.parseDouble(pLongitude);
			pLat  = Double.parseDouble(pLatitude);
			dLong = Double.parseDouble(dLongitude);
			dLat  = Double.parseDouble(dLatitude);
			
		} catch(NumberFormatException e){
			collector.ack(tuple);			
			return;
		}


		/* Do NOT emit if coordinates are outside the monitored grid */
		if (isOutsideGrid(pLat, pLong) || isOutsideGrid(dLat, dLong)){
			collector.ack(tuple);			
			return;
		}

		String dTimestamp = null;
		try {
			Date dDate = sdf.parse(dDatatime);
			dTimestamp = String.valueOf(dDate.getTime());
		} catch (ParseException e) {
			collector.ack(tuple);			
			return;
		}
		
		Values values = new Values();
		values.add(msgId);
		values.add(pDatatime); 	
		values.add(dDatatime); 	
		values.add(pLongitude); 	
		values.add(pLatitude); 		
		values.add(dLongitude); 	
		values.add(dLatitude); 		
		values.add(dTimestamp); 	
		values.add(timestamp);
		
		collector.emit(values);
		collector.ack(tuple);			

	}

    
    private boolean isOutsideGrid(double latitude, double longitude){

    	if (latitude > initialLatitude || 
        		latitude < finalLatitude ||
        		longitude < initialLongitude || 
        		longitude > finalLongitude)
        
        	return true;
        
    	return false;    	
    	
    }
    
    
    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    	
        outputFieldsDeclarer.declare(
        		new Fields(
        				F_MSGID,
						F_PICKUP_DATATIME,
						F_DROPOFF_DATATIME,
						F_PICKUP_LONGITUDE,
						F_PICKUP_LATITUDE,
						F_DROPOFF_LONGITUDE,
						F_DROPOFF_LATITUDE,
						F_DROPOFF_TIMESTAMP,
						F_TIMESTAMP));
        
    }

}