package it.uniroma2.debs2015gc;

import com.google.gson.Gson;
import it.uniroma2.debs2015gc.operator.Constants;
import it.uniroma2.debs2015gc.utils.LinesBatch;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class FakeDataSource implements Runnable {

	/*
	 * This datasource reads data from the debs2015 grand challenge
	 * and sends them trying to preserve inter-arrival times.
	 * 
	 * We define as real time, the time experienced by the data source
	 * while replying the data set. We define as event time, the time
	 * associated with each event stored in the data set, which should
	 * cover the whole 2013. 
	 * 
	 * We read the dataset with a granularity of TIMESPAN minutes at once, 
	 * i.e., we read all the events in TIMESPAN minutes of the simulated 
	 * time at once. 
	 * 
	 * Relying on TIMESPAN (expressed in minutes), it is possible to 
	 * accelerate the dataset by reading multiple events at once. 
	 * 
	 * Relying on SPEEDUP (expressed in milliseconds), it is possible to 
	 * define the (real) time interval between two consecutive minutes of 
	 * the simulated time. Using -1 as value, the dataset is reproduced
	 * with waiting times.
	 *
	 *  
	 */
	
	private static int SPEEDUP = 1000;		// expressed in ms 
//	private static int SHORT_SLEEP = 10;		// expressed in ms 
	
	private Jedis jedis;
	private int redisTimeout;
	private Gson gson;
	private int times;
	
	public FakeDataSource(int times, String redisUrl, int redisPort){

		this.times = times;
		this.jedis = new Jedis(redisUrl, redisPort, redisTimeout);
		this.gson = new Gson();

		initialize();
	}
	
	private void initialize(){
		jedis.del(Constants.REDIS_CONSUMED);
		jedis.del(Constants.REDIS_DATA);
	}
	
	@Override
	public void run() {

		LinesBatch linesBatch = null;
	    
		System.out.println("Initializing... ");
	
		String line_pref = "7166914A326D849158A5161E811ADDEA,FE44D0EA86E9882E9AAB77EB2DB868A4,";
		String line_m = ",300,0.91,";
		// -73.992805,40.734070,-73.981659,40.736805
		String line_suf  = ",CRD,5.50,0.50,0.50,1.20,0.00,7.70";

	    long latestSendingTime 	= System.currentTimeMillis();
	    double initialLatitude 	= 41.472691222;
	    double finalLatitude 	= (41.472691222 - 300.0 * 0.004491556); 	// 40.125224422
	    double initialLongitude = -74.916578;
	    double finalLongitude 	= (-74.91657 + 300.0 * 0.005986); 			// -73.12077
	    Random rnd = new Random();
	    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		long startingTime = 1359673200000l;
		int upd = 0;
		
		int burst = 0;
		long burstStart = System.currentTimeMillis();
		int MAX_BURST = 70000;
	    
	    while(true){
		
	    	linesBatch = new LinesBatch();
		
	    	long now = startingTime + upd * 60 * 1000;
	    	String pickup = sdf.format(new Date(now - rnd.nextInt(10*60*1000)));
	    	String dropoff = sdf.format(new Date(now));
	    	upd++;

			
			for (int i = 0; i < times; i++){

				double longit1 = rnd.nextDouble() * (finalLongitude - initialLongitude) + initialLongitude;
				double latit1 = rnd.nextDouble() * (initialLatitude - finalLatitude) + finalLatitude;
				
				double longit2 = rnd.nextDouble() * (finalLongitude - initialLongitude) + initialLongitude;
				double latit2 = rnd.nextDouble() * (initialLatitude - finalLatitude) + finalLatitude;

				String line = line_pref 
						+ pickup + "," + dropoff +
						line_m + 
						longit1 + "," + latit1 + "," +longit2 + "," + latit2 + line_suf;
				
				linesBatch.addLine(line);
					
			}
		    	
		    System.out.println("Sending " + linesBatch.getLines().size() + " lines -> " + dropoff );

		    /* batch is concluded and has to be sent */
		    send(linesBatch);					
		    
		    /* sleep if needed */
		    if (SPEEDUP != -1){
			    long deltaIntervalToSkip 	= SPEEDUP - (System.currentTimeMillis() - latestSendingTime);
			    
				System.out.println(" sleep for d:" + deltaIntervalToSkip );

				if (deltaIntervalToSkip < 0){
					System.out.println("WARNING: consumer is slower than source. A backpressure mechanism has been activated.");
					deltaIntervalToSkip = 0;
				}
				
			    try {
					Thread.sleep(deltaIntervalToSkip);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    	
		    }
		    
		    
		    burst++;
		    /* sleep if needed */
		    if (burst == MAX_BURST){
			    long deltaIntervalToSkip 	= 1000 - (System.currentTimeMillis() - burstStart);
			   			    
				System.out.println(" sleep for d:" + deltaIntervalToSkip );

				if (deltaIntervalToSkip < 0){
					System.out.println("WARNING: consumer is slower than source. A backpressure mechanism has been activated.");
					deltaIntervalToSkip = 0;
				}
				
			    try {
					Thread.sleep(deltaIntervalToSkip);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
		    	
			    
			    burst = 0;
			    burstStart = System.currentTimeMillis();
		    }
		    
		    
		    
		    latestSendingTime 	= System.currentTimeMillis();
			
		}

		
	}
	
	private void send(LinesBatch linesBatch) throws JedisConnectionException{

		String consumed = jedis.get(Constants.REDIS_CONSUMED);
		String data = jedis.get(Constants.REDIS_DATA);

		/* Check erroneous situation */
		if (data != null && consumed != null){

			jedis.del(Constants.REDIS_CONSUMED);
			jedis.del(Constants.REDIS_DATA);
			
		}

		/* Wait if the consumer is still reading data */
		if (data != null && consumed == null){

			while (consumed == null){
				
//				try {
//					Thread.sleep(SHORT_SLEEP);
//				} catch (InterruptedException e) { }
			
				consumed = jedis.get(Constants.REDIS_CONSUMED);
				
			}

		}

		/* Remove lock from Redis */
		jedis.del(Constants.REDIS_CONSUMED);

		/* Send data */
		String serializedBatch = gson.toJson(linesBatch);
		jedis.set(Constants.REDIS_DATA, serializedBatch);

	}

	/**
	 * This component reads data from the debs dataset
	 * and feeds the Storm topology by publishing data
	 * on Redis. 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		/* 
		 * Usage: 
		 * java -jar debs2015gc-1.0.jar it.uniroma2.debs2016gc.DataSource [batchSize] [speedup] [redis ip]
		 */
		
		int batchSize = 1;
		String redisHost = "localhost";

		try {
				
			if (args.length > 0) { batchSize = Integer.parseInt(args[0]); }
			if (args.length > 1) { SPEEDUP	 = Integer.parseInt(args[1]); }
			if (args.length > 2) { redisHost = args[2]; }
			
	    } catch (NumberFormatException e) {
	        System.err.println("Arguments " + args[0] + " and " + args[1] + " must be integers.");
	        System.err.println("Argument " +  args[2] + " must be a string.");
	    }
		
		FakeDataSource fill = new FakeDataSource(batchSize	, redisHost, 6379);
		Thread th1 = new Thread(fill);
		th1.start();
	}

}
