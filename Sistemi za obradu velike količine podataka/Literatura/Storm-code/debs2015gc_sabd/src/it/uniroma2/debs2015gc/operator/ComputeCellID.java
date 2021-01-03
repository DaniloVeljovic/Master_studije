package it.uniroma2.debs2015gc.operator;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class ComputeCellID extends BaseRichBolt {

	public static final String S_PULSE 				= "sPulse";
	public static final String F_MSGID				= "MSGID";
	public static final String F_PICKUP_DATATIME	= "pickupDatatime";
	public static final String F_DROPOFF_DATATIME 	= "dropoffDatatime";
	public static final String F_TIME 				= "time";
	public static final String F_ROUTE 				= "route";
	public static final String F_TIMESTAMP			= "timestamp";
	
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
     *
     *   To observe:
     *   For the challenge we allow a simplified flat earth assumption for
     *   mapping coordinates to cells in the queries. You can assume that 
     *   a distance of 500 meter south corresponds to a change of 0.004491556 
     *   degrees in the coordinate system. For moving 500 meter east you can 
     *   assume a change of 0.005986 degrees in the coordinate system. 
     *  
     */

    private double initialLatitude 	= 41.472691222;								// incr: s-n
    private double initialLongitude = -74.916578; 								// incr: w-e 
    private final double latitude500m 	= 0.004491556;
    private final double longitude500m 	= 0.005986;
	
	private static final long serialVersionUID = 1L;	
	private OutputCollector collector;

	public ComputeCellID(){

	}
	
    @Override
    public void prepare(@SuppressWarnings("rawtypes") Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        this.collector=outputCollector; 

    }
    
    @Override
	public void execute(Tuple tuple) {
    	
		String msgId 		= tuple.getStringByField(FilterByCoordinates.F_MSGID);
		String pDatatime 	= tuple.getStringByField(FilterByCoordinates.F_PICKUP_DATATIME);
		String pLongitude 	= tuple.getStringByField(FilterByCoordinates.F_PICKUP_LONGITUDE);
		String pLatitude  	= tuple.getStringByField(FilterByCoordinates.F_PICKUP_LATITUDE);
		String dDatatime 	= tuple.getStringByField(FilterByCoordinates.F_DROPOFF_DATATIME);
		String dLongitude 	= tuple.getStringByField(FilterByCoordinates.F_DROPOFF_LONGITUDE);
		String dLatitude  	= tuple.getStringByField(FilterByCoordinates.F_DROPOFF_LATITUDE);
		String dTime		= tuple.getStringByField(FilterByCoordinates.F_DROPOFF_TIMESTAMP);
		String timestamp	= tuple.getStringByField(FilterByCoordinates.F_TIMESTAMP);
		
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
		
		String pickupCell 	= computeCellId(pLat, pLong);
		String dropoffCell  = computeCellId(dLat, dLong);
		String route = pickupCell + "," + dropoffCell;
		
		Values values = new Values();
		values.add(msgId);
		values.add(pDatatime); 	
		values.add(dDatatime); 	 
		values.add(route);  	
		values.add(dTime);
		values.add(timestamp);
		
		collector.emit(values);
		collector.ack(tuple);			

	}
    
    private String computeCellId(double latitude, double longitude){
    	
    	int latId  = (int) Math.ceil((initialLatitude - latitude )   / latitude500m);
    	int longId = (int) Math.ceil((longitude - initialLongitude ) / longitude500m);
        
    	latId  = Math.max(1, Math.min(300,  latId));
    	longId = Math.max(1, Math.min(300, longId));
    	
    	String cellId = String.valueOf(longId) + "." + String.valueOf(latId);
        
        return cellId;
        
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    	
        outputFieldsDeclarer.declare(new Fields(F_MSGID, F_PICKUP_DATATIME, F_DROPOFF_DATATIME, F_ROUTE, F_TIME, F_TIMESTAMP));
    
    }

}