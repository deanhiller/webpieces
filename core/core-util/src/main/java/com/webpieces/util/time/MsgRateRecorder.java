package com.webpieces.util.time;

import java.util.concurrent.atomic.AtomicLong;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

/**
 * Records approximately every period but not exactly as it is more interested in printing the rate
 * of throughput
 * 
 * @author dhiller
 *
 */
public class MsgRateRecorder {

	private static final Logger log = LoggerFactory.getLogger(MsgRateRecorder.class);

	private int recordEveryPeriodMs;
	
	private AtomicLong counter = new AtomicLong();
	private long nextThreshold;
	private long previousTime;
	
	public MsgRateRecorder(int recordEveryPeriodSeconds) {
		this.recordEveryPeriodMs = recordEveryPeriodSeconds*1000;
		
		previousTime = System.currentTimeMillis();
		nextThreshold = previousTime+recordEveryPeriodMs;	
	}

	public void increment() {
		long total = counter.incrementAndGet();
		long newTime = System.currentTimeMillis();
		//log.info("receiving response. cnt="+total+" time="+newTime+" next threshold="+nextThreshold+" diff="+(nextThreshold-newTime));

//		if(total % 2000 == 0) {
//			log.info("received response");
//		}
		
		if(newTime > nextThreshold) {
			nextThreshold = newTime+recordEveryPeriodMs;
			counter.set(0);
			long timeDifference = (newTime - previousTime)/1000;
			double rps = ((double)total) / timeDifference;
			
			previousTime = newTime;
			//logged as error so it shows up in red ;)
			log.error("rps="+rps+" msg count="+total+" time="+timeDifference+" seconds");
		}		
	}
}
