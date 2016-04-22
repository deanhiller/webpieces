package org.webpieces.nio.api;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.futures.Future;

public class IntegTestLocalhostServerListener implements DataListener {
	private static final Logger log = LoggerFactory.getLogger(IntegTestLocalhostServerListener.class);
	private BufferCreationPool pool;

	public IntegTestLocalhostServerListener(BufferCreationPool pool) {
		this.pool = pool;
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		Future<Channel, FailureInfo> future = channel.write(b);
		
		future.setCancelFunction(p -> finished("cancelled", null, b))
			.setResultFunction(p -> finished("data written", null, b))
			.setFailureFunction(p -> finished("failure", p, b));
	}

	private void finished(String string, FailureInfo p, ByteBuffer buffer) {
		pool.releaseBuffer(buffer);
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
