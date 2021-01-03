package it.uniroma2.debs2015gc.operator;

import org.apache.storm.task.OutputCollector;
import org.apache.storm.task.TopologyContext;
import org.apache.storm.topology.OutputFieldsDeclarer;
import org.apache.storm.topology.base.BaseRichBolt;
import org.apache.storm.tuple.Fields;
import org.apache.storm.tuple.Tuple;
import org.apache.storm.tuple.Values;

import java.util.Map;

public class ParseLine extends BaseRichBolt {

	public static final String F_MSGID				= "MSGID";
	public static final String F_PICKUP_DATATIME	= "pickupDatatime";
	public static final String F_DROPOFF_DATATIME 	= "dropoffDatatime";
	public static final String F_PICKUP_LONGITUDE 	= "pickupLongitude";
	public static final String F_PICKUP_LATITUDE	= "pickupLatitude";
	public static final String F_DROPOFF_LONGITUDE 	= "dropoffLongitude";
	public static final String F_DROPOFF_LATITUDE	= "dropoffLatitude";
	public static final String F_TIMESTAMP			= "timestamp";

	
	private static final long serialVersionUID = 1L;	
	private OutputCollector collector;

	
	public ParseLine(){
    }

	
    @Override
    public void prepare(@SuppressWarnings("rawtypes") Map map, TopologyContext topologyContext, OutputCollector outputCollector) {

        this.collector=outputCollector; 

    }

    /* 
     * DEBS 2015 GC Data Format: 
     * 
     *    0    medallion     		an md5sum of the identifier of the taxi - vehicle bound
     *    1    hack_license  		an md5sum of the identifier for the taxi license
     *    2    pickup_datetime      time when the passenger(s) were picked up
     *    3    dropoff_datetime     time when the passenger(s) were dropped off
	 *    4    trip_time_in_secs    duration of the trip
	 *    5    trip_distance trip 	distance in miles
	 *    6    pickup_longitude     longitude coordinate of the pickup location
 	 *    7    pickup_latitude      latitude coordinate of the pickup location
	 *    8    dropoff_longitude    longitude coordinate of the drop-off location
	 *    9    dropoff_latitude     latitude coordinate of the drop-off location
	 *   10    payment_type  		the payment method - credit card or cash
	 *   11    fare_amount   		fare amount in dollars
	 *   12    surcharge     		surcharge in dollars
	 *   13    mta_tax       		tax in dollars
	 *   14    tip_amount    		tip in dollars
	 *   15    tolls_amount  		bridge and tunnel tolls in dollars
	 *   16    total_amount  		total paid amount in dollars	 
     *  
     */

    @Override
	public void execute(Tuple tuple) {
    	
		String rawData 	= tuple.getStringByField(RedisSpout.F_DATA);
		String msgId 	= tuple.getStringByField(RedisSpout.F_MSGID);
		String timestamp = tuple.getStringByField(RedisSpout.F_TIMESTAMP);
		
		
		/* Do NOT emit if the EOF has been reached */
		if (rawData == null || rawData.equals(Constants.REDIS_EOF)){
			collector.ack(tuple);			
			return;
		}
		
		/* Do NOT emit if the EOF has been reached */
		String[] data = rawData.split(",");
		if (data == null || data.length != 17){
			collector.ack(tuple);			
			return;
		}
		
		Values values = new Values();
		values.add(msgId);
		values.add(data[2]); // pickupDataTime
		values.add(data[3]); // dropoffDataTime
		values.add(data[6]); // pickupLongitude
		values.add(data[7]); // pickupLatitude
		values.add(data[8]); // dropoffLongitude
		values.add(data[9]); // dropoffLatitude
		values.add(timestamp);
		
		collector.emit(values);
		collector.ack(tuple);			

	}

    @Override
    public void declareOutputFields(OutputFieldsDeclarer outputFieldsDeclarer) {
    	
        outputFieldsDeclarer.declare(new Fields(F_MSGID, F_PICKUP_DATATIME, F_DROPOFF_DATATIME, F_PICKUP_LONGITUDE, F_PICKUP_LATITUDE, F_DROPOFF_LONGITUDE, F_DROPOFF_LATITUDE, F_TIMESTAMP));
        
    }

}