package it.uniroma2.debs2015gc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import it.uniroma2.debs2015gc.utils.LinesBatch;
import redis.clients.jedis.exceptions.JedisConnectionException;

public class DatasetProfiler {

	private static final int TIMESPAN = 1; 		// expressed in mins 
//	private static final int SPEEDUP = 1000; 	// expressed in ms 
	private static final int SPEEDUP = -1;
	
	private SimpleDateFormat sdf;
	private String filename;
	private BufferedWriter bw = null; 
	
	public DatasetProfiler(String filename){

		this.filename = filename;
		this.sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		FileWriter fw;
		try {
			fw = new FileWriter("profile", true);
			bw = new BufferedWriter(fw);
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	public void run() {

		BufferedReader br = null;
		LinesBatch linesBatch = null;
	    
		try {
			System.out.println("Initialization");
			br = new BufferedReader(new FileReader(filename));
		
			String line = br.readLine();
		    linesBatch = new LinesBatch();
		    long batchInitialTime 	= roundToCompletedMinute(getDropoffDatatime(line));
		    long batchFinalTime 	= computeBatchFinalTime(batchInitialTime);
		    long latestSendingTime 	= System.currentTimeMillis();
		    linesBatch.addLine(line);
		    
			System.out.println("Running ... ");
		    
		    while ((line = br.readLine()) != null) {

			    long eventTime = getDropoffDatatime(line);
			    
			    if (eventTime < batchFinalTime){
			    	linesBatch.addLine(line);
			    	continue;
			    }
			    
			    /* batch is concluded and has to be sent */
			    send(linesBatch);					
			    
			    /* sleep if needed */
//			    if (SPEEDUP != -1){
//				    long nextBatchInitTime = roundToCompletedMinute(eventTime);
//				    long completeIntervalToSkip = SPEEDUP * (int) Math.floor(((double) (nextBatchInitTime - batchFinalTime) / (TIMESPAN * 60 * 1000)));
//				    long deltaIntervalToSkip 	= SPEEDUP - (System.currentTimeMillis() - latestSendingTime);
//				    
//					System.out.println(" sleep for d:" + deltaIntervalToSkip + " + c:" +completeIntervalToSkip);
//
//					if (deltaIntervalToSkip < 0){
//						System.out.println("WARNING: consumer is slower than source. A backpressure mechanism has been activated.");
//						deltaIntervalToSkip = 0;
//					}
//					
//				    try {
//						Thread.sleep(deltaIntervalToSkip + completeIntervalToSkip);
//					} catch (InterruptedException e) {
//						e.printStackTrace();
//					}
//			    	
//			    }
			    
			    /* update batch */
			    linesBatch = new LinesBatch();
			    linesBatch.addLine(line);
			    batchInitialTime = roundToCompletedMinute(eventTime);
			    batchFinalTime 	= computeBatchFinalTime(batchInitialTime);
			    latestSendingTime 	= System.currentTimeMillis();

				System.out.println(" current (simulation) time " + sdf.format(new Date(batchFinalTime)));
		    	
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if (br != null){
			try {
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
	}
	
	private void send(LinesBatch linesBatch) throws JedisConnectionException{

		long start = 0;
		long end = start; 
		
		if (!linesBatch.getLines().isEmpty()){
			start = getDropoffDatatime(linesBatch.getLines().get(0));
			end = getDropoffDatatime(linesBatch.getLines().get(linesBatch.getLines().size() - 1));
		}
		
		String line = "";
		line += System.currentTimeMillis() + ", ";
		line += start + ", " + end + ", ";
		line += linesBatch.getLines().size();
		line += " \n";
		
		if (bw != null){
			try {
				bw.write(line);
				bw.flush();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}
		
	private long getDropoffDatatime(String line){

		long ts = 0;
		
    	try {
    		String[] tokens	=	line.split(",");
			Date dropoff = sdf.parse(tokens[3]);
			ts = dropoff.getTime();
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
    	
    	return ts;
    	
	}

    private long roundToCompletedMinute(long timestamp) {

		Date d = new Date(timestamp);
		Calendar date = new GregorianCalendar();
		date.setTime(d);
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);

		return date.getTime().getTime();
	
	}
    
	private long computeBatchFinalTime(long initialTime){
		
		return initialTime + TIMESPAN * 60 * 1000;
	}
	
	
	/**
	 * This component reads data from the debs dataset
	 * and feeds the Storm topology by publishing data
	 * on Redis. 
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		
		/* TODO: 
		 * Usage: 
		 * java -jar debs2015gc-1.0.jar it.uniroma2.debs2016gc.DataSource [debs dataset] [redis ip] 
		 */
		
		String file = "/home/matteo/Downloads/debs2015/debs2015_sample.csv";
		DatasetProfiler fill = new DatasetProfiler(file);
		fill.run();
	}

}
