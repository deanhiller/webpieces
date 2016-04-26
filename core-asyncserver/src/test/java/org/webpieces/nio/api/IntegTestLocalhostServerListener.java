package org.webpieces.nio.api;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.futures.Failure;

public class IntegTestLocalhostServerListener implements DataListener {
	private static final Logger log = LoggerFactory.getLogger(IntegTestLocalhostServerListener.class);
	private BufferCreationPool pool;

	public IntegTestLocalhostServerListener(BufferCreationPool pool) {
		this.pool = pool;
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		CompletableFuture<Channel> future = channel.write(b);
		
		future
			.thenAccept(p -> finished("data written", null, b))
			.whenComplete((r, e) -> fail(channel, b, r, e));
	}

	private void finished(String string, Failure p, ByteBuffer buffer) {
		pool.releaseBuffer(buffer);
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

}
