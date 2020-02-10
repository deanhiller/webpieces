package org.webpieces.nio.api;

public class BackpressureConfig {

	/**
	 * The maximum bytes that can be unacked.  After this, we stop reading from the socket putting backpressure on the
	 * remote end that is talking to you.  
	 * 
	 * setting this to null turns backpressure off and will just slam you with data non-stop as it comes in
	 * 
	 * Generally, it's bad to have a client backpressure a server and only the server will backpressure the clients
	 */
	private Integer maxBytes = 8_192*8;
	
	/**
	 * Rather than thrashing turning on and off reading (if you hit the maxBytes that is), it is better to consume for a while
	 * and catch up and turn it back on at some lower threshold.
	 * 
	 * It is very important that this is NOT 0!!!  As the app, acks messages, it will ack the corresponding size of the message BUT
	 * there may be half a message that cannot be acked by the app as it can only ack a full message.  (ie. the size here needs to
	 * be larger than the largest message size).  Otherwise, you can set this to null and thrash.  Http/2 can set the max message
	 * size so this should be set for http/2
	 */
	private Integer startReadingThreshold = 8_192*2;

	public Integer getMaxBytes() {
		return maxBytes;
	}

	public void setMaxBytes(Integer maxBytes) {
		this.maxBytes = maxBytes;
	}

	public Integer getStartReadingThreshold() {
		return startReadingThreshold;
	}

	public void setStartReadingThreshold(Integer startReadingThreshold) {
		this.startReadingThreshold = startReadingThreshold;
	}

}
