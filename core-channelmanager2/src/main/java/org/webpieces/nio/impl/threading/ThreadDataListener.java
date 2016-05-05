package org.webpieces.nio.impl.threading;

import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.threading.SessionExecutor;

public class ThreadDataListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(ThreadDataListener.class);
	private DataListener dataListener;
	private SessionExecutor executor;

	public ThreadDataListener(DataListener dataListener, SessionExecutor executor) {
		this.dataListener = dataListener;
		this.executor = executor;
	}

	@Override
	public void incomingData(Channel channel, ByteBuffer b) {
		log.info("throwing data from channel="+channel+" into queue to be processed");
		executor.execute(channel, new Runnable() {
			@Override
			public void run() {
				dataListener.incomingData(channel, b);
			}
		});
	}

	@Override
	public void farEndClosed(Channel channel) {
		executor.execute(channel, new Runnable() {
			@Override
			public void run() {
				dataListener.farEndClosed(channel);
			}
		});
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		executor.execute(channel, new Runnable() {
			@Override
			public void run() {
				dataListener.failure(channel, data, e);
			}
		});
	}

	@Override
	public void applyBackPressure(Channel channel) {
		//short circuit such that they can kill backpressure sooner
		new Runnable() {
			@Override
			public void run() {
				dataListener.applyBackPressure(channel);
			}
		}.run();;
	}

	@Override
	public void releaseBackPressure(Channel channel) {
		//short circuit such that they can kill backpressure sooner
		//and because applyBackpressure was short circuited, this ensures ordering
		//between both methods
		new Runnable() {
			@Override
			public void run() {
				dataListener.releaseBackPressure(channel);
			}
		};
	}

}
