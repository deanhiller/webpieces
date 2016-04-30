package org.webpieces.httpproxy.impl.chain;

import java.nio.ByteBuffer;
import java.util.concurrent.Executor;

import javax.inject.Inject;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class Layer1aDataListener implements DataListener {

	@Inject
	private Layer2DataListener listener;
	@Inject
	private Executor executor;
	
	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				listener.incomingData(channel, b);
			}
		});
	}

	@Override
	public void farEndClosed(Channel channel) {
		executor.execute(new Runnable() {
			@Override
			public void run() {
				listener.farEndClosed(channel);
			}
		});
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		executor.execute(new Runnable() {
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
