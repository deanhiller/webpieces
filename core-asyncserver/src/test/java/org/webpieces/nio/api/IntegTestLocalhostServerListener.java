package org.webpieces.nio.api;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

import com.webpieces.data.api.BufferPool;

public class IntegTestLocalhostServerListener implements DataListener {
	private static final Logger log = LoggerFactory.getLogger(IntegTestLocalhostServerListener.class);
	private BufferPool pool;

	private int counter = 0;
	
	public IntegTestLocalhostServerListener(BufferPool pool) {
		this.pool = pool;
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		counter++;
		if(counter % 1000 == 0)
			log.info("still receiving");
		
		CompletableFuture<Channel> future = channel.write(b);
		
		future
			.thenAccept(p -> finished("data written", b))
			.whenComplete((r, e) -> fail(channel, b, r, e));
	}

	private void finished(String string, ByteBuffer buffer) {
	}

	private void fail(Channel channel, ByteBuffer b, Void r, Throwable e) {
		if(e != null) {
			log.info("finished exception="+e, e);
			pool.releaseBuffer(b);
			channel.close();
		}
	}
	
	@Override
	public void farEndClosed(Channel channel) {
		log.info("far end closed");
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.info("failure on processing", e);
	}

	@Override
	public void applyBackPressure(Channel channel) {
		log.info("server apply backpressure");
		channel.unregisterForReads();
	}

	@Override
	public void releaseBackPressure(Channel channel) {
		log.info("server releasing backpressure");
		channel.registerForReads();
		log.info("registered for reads");
	}
}
