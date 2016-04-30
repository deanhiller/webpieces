package org.webpieces.nio.api;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.futures.Failure;

import com.webpieces.data.api.BufferPool;

public class IntegTestClientNotReadListener implements DataListener {
	private static final Logger log = LoggerFactory.getLogger(IntegTestClientNotReadListener.class);
	private BufferPool pool;

	public IntegTestClientNotReadListener(BufferPool pool) {
		this.pool = pool;
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		log.info("receiving data server..writing now");
		CompletableFuture<Channel> future = channel.write(b);

		future
			.thenAccept(p -> finished("data written", null, b))
			.exceptionally(e -> fail(channel, e));
	}

	private Void fail(Channel channel, Throwable e) {
		log.info("finished exception="+e, e);
		if(!channel.isClosed()) {
			CompletableFuture<Channel> close = channel.close();
			close.thenAccept(p -> {
				log.info("Channel closed");
			});
		}
		return null;
	}

	private void finished(String string, Failure p, ByteBuffer buffer) {
		log.info("writing finished reason="+string);
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

	@Override
	public void applyBackPressure(Channel channel) {
		log.info("server unregistering for reads");
		channel.unregisterForReads();
	}

	@Override
	public void releaseBackPressure(Channel channel) {
		log.info("server registring for reads");
		channel.registerForReads();
	}
}
