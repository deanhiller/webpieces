package org.webpieces.nio.impl.ssl;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class SslTryCatchListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(SslTryCatchListener.class);
	
	private DataListener listener;

	private boolean closedAlready;

	public SslTryCatchListener(DataListener listener) {
		this.listener = listener;
	}

	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		try {
			return listener.incomingData(channel, b);
		} catch (Throwable e) {
			log.error("Exception", e);
			CompletableFuture<Void> future = new CompletableFuture<>();
			future.completeExceptionally(e);
			return future;
		}
	}

	public void farEndClosed(Channel channel) {
		try {
			synchronized(this) {
				if(closedAlready)
					return;
				closedAlready = true;
			}
			listener.farEndClosed(channel);
		} catch (Throwable e) {
			log.error("Exception", e);
		}
	}

	public void failure(Channel channel, ByteBuffer data, Exception e) {
		try {
			listener.failure(channel, data, e);
		} catch (Throwable ee) {
			log.error("Exception processing other exception", ee);
		}
	}

}
