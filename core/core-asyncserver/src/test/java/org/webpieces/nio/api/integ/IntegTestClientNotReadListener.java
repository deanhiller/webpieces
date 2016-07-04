package org.webpieces.nio.api.integ;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class IntegTestClientNotReadListener implements DataListener {
	private static final Logger log = LoggerFactory.getLogger(IntegTestClientNotReadListener.class);

	public IntegTestClientNotReadListener() {
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		log.info("receiving data server..writing now");
		CompletableFuture<Channel> future = channel.write(b);

		future
			.thenAccept(p -> finished("data written", b))
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

	private void finished(String string, ByteBuffer buffer) {
		log.info("writing finished reason="+string);
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
