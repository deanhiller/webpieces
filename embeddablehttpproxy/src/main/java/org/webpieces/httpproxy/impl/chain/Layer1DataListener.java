package org.webpieces.httpproxy.impl.chain;

import java.nio.ByteBuffer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.threading.SessionExecutor;

@Singleton
public class Layer1DataListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(Layer1DataListener.class);
	
	@Inject
	private Layer2DataListener listener;
	@Inject
	private SessionExecutor executor;
	
	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		log.info("incoming data="+b.remaining());
		executor.execute(channel, new Runnable() {
			@Override
			public void run() {
				listener.incomingData(channel, b);
			}
		});
	}

	@Override
	public void farEndClosed(Channel channel) {
		executor.execute(channel, new Runnable() {
			@Override
			public void run() {
				listener.farEndClosed(channel);
			}
		});
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		executor.execute(channel, new Runnable() {
			@Override
			public void run() {
				listener.failure(channel, data, e);
			}
		});
	}

	@Override
	public void applyBackPressure(Channel channel) {
	}

	@Override
	public void releaseBackPressure(Channel channel) {
	}

}
