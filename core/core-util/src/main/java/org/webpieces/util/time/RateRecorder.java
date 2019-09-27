package org.webpieces.util.time;

import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Records approximately every period but not exactly as it is more interested in printing the rate
 * of throughput
 * 
 * @author dhiller
 *
 */
public class RateRecorder {

	private static final Logger log = LoggerFactory.getLogger(RateRecorder.class);

	private int recordEveryPeriodMs;
	
	private AtomicLong counter = new AtomicLong();
	private long nextThreshold;
	private long previousTime;
	private long previousCount;
	private String postfix;
	private double scaleReduction;

	public RateRecorder(int recordEveryPeriodSeconds) {
		this(recordEveryPeriodSeconds, "requests/second", 1);
	}
	
	public RateRecorder(int recordEveryPeriodSeconds, String postfix, double scaleReduction) {
		this.postfix = postfix;
		this.scaleReduction = scaleReduction;
		this.recordEveryPeriodMs = recordEveryPeriodSeconds*1000;
		
		previousTime = System.currentTimeMillis();
		nextThreshold = previousTime+recordEveryPeriodMs;	
	}

	public void increment() {
		increment(1);
	}
	
	public void increment(int incrementBy) {
//		long totalNow = counter.addAndGet(incrementBy);
//
//		boolean logRate = false;
//		long newTime;
//		synchronized(this) {
//			newTime = System.currentTimeMillis();
//			if(newTime > nextThreshold)
//				logRate = true;
//		}
//		
//		if(logRate) {
//			nextThreshold = newTime+recordEveryPeriodMs;
//			long total = totalNow - previousCount;
//			previousCount = totalNow;  //reset previousCount
//			long timeDifference = (newTime - previousTime)/1000;
//			double rps = ((double)total) / timeDifference;
//			
//			previousTime = newTime;
//			//logged as error so it shows up in red ;)
//			double newRps = rps / scaleReduction;
//			log.error("rate="+newRps+" "+postfix+" total count="+total+" total time="+timeDifference+" seconds");
//		}		
	}


}
