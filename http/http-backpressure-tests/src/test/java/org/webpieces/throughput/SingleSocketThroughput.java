package org.webpieces.throughput;

import org.webpieces.frontend2.api.Protocol;
import org.webpieces.nio.api.BackpressureConfig;

public class SingleSocketThroughput {

	public static void main(String[] args) throws InterruptedException {
		//All of these are variables that can impact performance so we surface them here to play with
		BackpressureConfig backpressureConfig = new BackpressureConfig();
		//num unacked bytes (client did not ack yet) before we backpressure
		backpressureConfig.setMaxBytes(8_192*8); 
		//once backpressure is on, instead of flapping, we wait for client to catch up quite a bit(may increase performance in certain use cases)
		//or at the very least create more fairness if one client is giving more requests than another
		backpressureConfig.setStartReadingThreshold(8_192*2); 

		AsyncConfig config = new AsyncConfig();
		config.setClientThreadCount(null); //turns off mulithreaded but useless for this test!!! since there is only one socket
		config.setServerThreadCount(null); //turns off mulithreaded but useless for this test!!! since there is only one socket
		config.setHttps(false);
		config.setBackPressureConfig(backpressureConfig);
		
		//this setting only applies to http2 server/client pair...
		config.setHttp2ClientMaxConcurrentRequests(200);
		
		ThroughputEngine example = new ThroughputEngine(config);
		
		//BIG NOTE: It is not fair to set multiThreaded=true BECAUSE in that case you would need
		//many many sockets because the threadpool causes context switching BUT keeps the one socket
		//virtually single threaded(this is a BIG pretty awesome feature actually).  The REASON it keeps
		//it virtually single threaded is so that SSL handshaking, http2 parsing, http1.1 parsing can
		//all be delayed until the thread pool.  BUT we leave the switch here so we can play with it
		//AND we need to code up another rps example for many many sockets

		//yes, 8 combinations that can be tried (as we wanted to compare old IO while we were doing this)
//		example.start(Mode.ASYNCHRONOUS, Mode.ASYNCHRONOUS, Protocol.HTTP11);
//		example.start(Mode.SYNCHRONOUS, Mode.SYNCHRONOUS, Protocol.HTTP11);
//		example.start(Mode.SYNCHRONOUS, Mode.ASYNCHRONOUS, Protocol.HTTP11);
//		example.start(Mode.ASYNCHRONOUS, Mode.SYNCHRONOUS, Protocol.HTTP11);
//		

		example.start(Mode.ASYNCHRONOUS, Mode.ASYNCHRONOUS, Protocol.HTTP2);
		
		//NOTE: Synchronous is HARD to implement and must use the http2 engine to do it properly.
		//This is an exercise for later.....
//		example.start(Mode.SYNCHRONOUS, Mode.SYNCHRONOUS, Protocol.HTTP2);
//		example.start(Mode.SYNCHRONOUS, Mode.ASYNCHRONOUS, Protocol.HTTP2);		
//		example.start(Mode.SYNCHRONOUS, Mode.ASYNCHRONOUS, Protocol.HTTP2);
		
	}
	

}
