package org.webpieces.nio.impl.cm.basic;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;


public class WriteRunnable {

	private static final Logger log = LoggerFactory.getLogger(WriteRunnable.class);
	private ByteBuffer buffer;
	private CompletableFuture<Channel> handler;
	private BasChannelImpl channel;
	private long creationTimestamp;

	public WriteRunnable(BasChannelImpl c, ByteBuffer b, CompletableFuture<Channel> impl, long creationTimestamp) {
		channel = c;
		buffer = b;
		handler = impl;
		this.creationTimestamp = creationTimestamp;
	}

	public boolean runDelayedAction() {
		if(!buffer.hasRemaining())
			throw new IllegalStateException("Trying to write out empty buffer");
		
		int wroteOut = channel.writeImpl(buffer);
		
		if(log.isTraceEnabled())
			log.trace("wrote out bytes="+wroteOut+" still remaining="+buffer.remaining());

		if(buffer.hasRemaining())
			return false;
              
		return true;
	}

	public long getCreationTime() {
		return creationTimestamp;
	}

	public CompletableFuture<Channel> getPromise() {
		return handler;
	}

	public void markBufferRead() {
		buffer.position(buffer.limit());
	}

}
