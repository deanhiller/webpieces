package org.webpieces.nio.impl.threading;

import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.cm.basic.MDCUtil;
import org.webpieces.util.threading.SessionExecutor;

public class ThreadConnectionListener implements ConnectionListener {

	private ConnectionListener connectionListener;
	private SessionExecutor executor;

	public ThreadConnectionListener(ConnectionListener connectionListener, SessionExecutor executor) {
		this.connectionListener = connectionListener;
		this.executor = executor;
	}

	@Override
	public XFuture<DataListener> connected(Channel channel, boolean isReadyForWrites) {
		XFuture<DataListener> future = new XFuture<DataListener>();

		ThreadTCPChannel proxy = new ThreadTCPChannel((TCPChannel) channel, executor);

		ThreadConnectRunnable runnable = new ThreadConnectRunnable(executor, channel, connectionListener, proxy, isReadyForWrites, future);
		
		executor.execute(proxy, runnable);
		
		return future;
	}

	private static class ThreadConnectRunnable implements Runnable {

		private Channel channel;
		private ConnectionListener connectionListener;
		private ThreadTCPChannel proxy;
		private boolean isReadyForWrites;
		private XFuture<DataListener> future;
		private SessionExecutor executor;

		public ThreadConnectRunnable(SessionExecutor executor, Channel channel, ConnectionListener connectionListener, ThreadTCPChannel proxy,
				boolean isReadyForWrites, XFuture<DataListener> future) {
					this.executor = executor;
					this.channel = channel;
					this.connectionListener = connectionListener;
					this.proxy = proxy;
					this.isReadyForWrites = isReadyForWrites;
					this.future = future;
		}

		@Override
		public void run() {
			MDCUtil.setMDC(channel.isServerSide(), channel.getChannelId());
			try {
				XFuture<DataListener> dataListener = connectionListener.connected(proxy, isReadyForWrites);
				//transfer the listener to the future to be used
				dataListener
					.thenAccept(listener -> translate(proxy, future, listener))
					.exceptionally(e -> {
						future.completeExceptionally(e);
						return null;
					});
			} finally {
				MDCUtil.clearMDC(channel.isServerSide());
			}
		}
		
		private void translate(ThreadTCPChannel proxy, XFuture<DataListener> future, DataListener listener) {
			DataListener wrappedDataListener = new ThreadDataListener(proxy, listener, executor);
			future.complete(wrappedDataListener);
		}
	}
	

	
	@Override
	public void failed(RegisterableChannel channel, Throwable e) {
		Runnable runnable = new Runnable() {
			@Override
			public void run() {
				connectionListener.failed(channel, e);
			}
		};
		
		executor.execute(channel, new SessionRunnable(runnable, channel));
	}
}
