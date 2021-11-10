package org.webpieces.nio.impl.ssl;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class SslTryCatchListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(SslTryCatchListener.class);
	
	private DataListener listener;

	private boolean closedAlready;

	public SslTryCatchListener(DataListener listener) {
		this.listener = listener;
	}

	public XFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		try {
			return listener.incomingData(channel, b);
		} catch (Throwable e) {
			log.error("Exception", e);
			XFuture<Void> future = new XFuture<>();
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
