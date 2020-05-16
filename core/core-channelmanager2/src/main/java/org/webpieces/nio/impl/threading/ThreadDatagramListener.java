package org.webpieces.nio.impl.threading;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.handlers.DatagramListener;
import org.webpieces.util.threading.SessionExecutor;

public class ThreadDatagramListener implements DatagramListener {


	private DatagramListener listener;
	private SessionExecutor executor;

	public ThreadDatagramListener(DatagramListener listener, SessionExecutor executor) {
		this.listener = listener;
		this.executor = executor;
	}

	@Override
	public void incomingData(DatagramChannel channel, InetSocketAddress fromAddr, ByteBuffer b) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				listener.incomingData(channel, fromAddr, b);
			}
		};
		
		executor.execute(channel, new SessionRunnable(runnable, channel));
	}

	@Override
	public void failure(DatagramChannel channel, InetSocketAddress fromAddr, ByteBuffer data, Throwable e) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				listener.failure(channel, fromAddr, data, e);
			}
		};
		
		executor.execute(channel, new SessionRunnable(runnable, channel));
	}
	

}
