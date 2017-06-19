package org.webpieces.throughput;

import org.webpieces.frontend2.api.Protocol;

public class SingleSocketThroughput {

	public static void main(String[] args) throws InterruptedException {
		ThroughputEngine example = new ThroughputEngine();
		
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
