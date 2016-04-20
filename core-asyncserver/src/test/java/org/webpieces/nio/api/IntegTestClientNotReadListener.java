package org.webpieces.nio.api;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.futures.Future;

public class IntegTestClientNotReadListener implements DataListener {
	private static final Logger log = LoggerFactory.getLogger(IntegTestClientNotReadListener.class);
	private BufferCreationPool pool;

	public IntegTestClientNotReadListener(BufferCreationPool pool) {
		this.pool = pool;
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		Future<Channel, FailureInfo> future = channel.write(b);
		
		future.setCancelFunction(p -> finished("cancelled", null, b))
			.setResultFunction(p -> finished("data written", null, b))
			.setFailureFunction(p -> fail(channel, "failure", p, b));
	}

	private void fail(Channel channel, String string, FailureInfo p, ByteBuffer b) {
		log.info("finished exception="+string, p.getException());
		pool.releaseBuffer(b);
		channel.close();
	}

	private void finished(String string, FailureInfo p, ByteBuffer buffer) {
		log.info("finished reason="+string);
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
