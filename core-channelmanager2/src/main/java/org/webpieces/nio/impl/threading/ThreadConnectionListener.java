package org.webpieces.nio.impl.threading;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.util.threading.SessionExecutor;

public class ThreadConnectionListener implements ConnectionListener {

	private ConnectionListener connectionListener;
	private SessionExecutor executor;

	public ThreadConnectionListener(ConnectionListener connectionListener, SessionExecutor executor) {
		this.connectionListener = connectionListener;
		this.executor = executor;
	}

	@Override
	public void connected(Channel channel) {
		executor.execute(channel, new Runnable() {
			@Override
			public void run() {
				ThreadTCPChannel proxy = new ThreadTCPChannel((TCPChannel) channel, executor);
				connectionListener.connected(proxy);
			}
		});
	}

	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
		executor.execute(channel, new Runnable() {
			@Override
			public void run() {
				connectionListener.failed(channel, e);
			}
		});
	}
}
