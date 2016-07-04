package org.webpieces.nio.impl.ssl;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class SslTryCatchListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(SslTryCatchListener.class);
	
	private DataListener listener;

	public SslTryCatchListener(DataListener listener) {
		this.listener = listener;
	}

	public void incomingData(Channel channel, ByteBuffer b, boolean isOpeningConnection) {
		try {
			listener.incomingData(channel, b, isOpeningConnection);
		} catch (Throwable e) {
			log.error("Exception", e);
		}
	}

	public void farEndClosed(Channel channel) {
		try {
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

	public void applyBackPressure(Channel channel) {
		try {
			listener.applyBackPressure(channel);
		} catch (Throwable e) {
			log.error("Exception", e);
		}
	}

	public void releaseBackPressure(Channel channel) {
		try {
			listener.releaseBackPressure(channel);
		} catch (Throwable e) {
			log.error("Exception", e);
		}
	}

}
