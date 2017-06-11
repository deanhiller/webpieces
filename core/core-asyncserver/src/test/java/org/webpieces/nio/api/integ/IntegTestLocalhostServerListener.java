package org.webpieces.nio.api.integ;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class IntegTestLocalhostServerListener implements AsyncDataListener {
	private static final Logger log = LoggerFactory.getLogger(IntegTestLocalhostServerListener.class);

	public IntegTestLocalhostServerListener() {
	}

	@Override
	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		return channel.write(b)
				.thenAccept(p -> finished("data written", b));
	}

	private void finished(String string, ByteBuffer buffer) {
	}

	@Override
	public void connectionOpened(TCPChannel proxy, boolean isReadyForWrites) {
		log.info("opened connection="+proxy);
	}
	
	@Override
	public void farEndClosed(Channel channel) {
		log.info("far end closed="+channel);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.info("failure on processing", e);
	}

}
