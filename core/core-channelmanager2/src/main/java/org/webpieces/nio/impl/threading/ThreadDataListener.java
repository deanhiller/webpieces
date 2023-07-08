package org.webpieces.nio.impl.threading;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.impl.cm.basic.MDCUtil;
import org.webpieces.util.threading.SessionExecutor;

public class ThreadDataListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(ThreadDataListener.class);
	private DataListener dataListener;
	private SessionExecutor executor;
	//this must be sent in case people compare objects since this is what is sent through the 'connected' method.
	private ThreadChannel proxy;

	public ThreadDataListener(ThreadChannel proxy, DataListener dataListener, SessionExecutor executor) {
		this.proxy = proxy;
		this.dataListener = dataListener;
		this.executor = executor;
	}

	@Override
	public XFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		XFuture<Void> future = new XFuture<Void>();
		executor.execute(proxy, new DataListeneRunanble(dataListener, proxy, b, future));
		
		return future;
	}

	private static class DataListeneRunanble implements Runnable {
		private DataListener dataListener;
		private ThreadChannel proxy;
		private ByteBuffer buffer;
		private XFuture<Void> future;

		public DataListeneRunanble(DataListener dataListener, ThreadChannel proxyChannel, ByteBuffer b,
				XFuture<Void> future) {
					this.dataListener = dataListener;
					this.proxy = proxyChannel;
					this.buffer = b;
					this.future = future;
		}

		@Override
		public void run() {
			MDCUtil.setMDC(proxy.isServerSide(), proxy.getChannelId());

			try {
				XFuture<Void> fut = dataListener.incomingData(proxy, buffer);
				fut.handle((v, t) -> {
					if(t == null)
						future.complete(null);
					else
						future.completeExceptionally(t);
					return null;
				});
				
			} catch(Throwable e) {
				log.error("Uncaught Exception", e);
				future.completeExceptionally(e);
			} finally {
				MDCUtil.clearMDC(proxy.isServerSide());
			}
		}
	}
	
	@Override
	public void farEndClosed(Channel channel) {
		executor.execute(proxy, new Runnable() {
			@Override
			public void run() {
				MDCUtil.setMDC(proxy.isServerSide(), proxy.getChannelId());
				try {
					dataListener.farEndClosed(proxy);
				} catch(RuntimeException e) {
					throw e;
				} finally {
					MDCUtil.setMDC(proxy.isServerSide(), proxy.getChannelId());
				}
			}
		});
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		executor.execute(proxy, new Runnable() {
			@Override
			public void run() {
				MDCUtil.setMDC(proxy.isServerSide(), proxy.getChannelId());
				try {
					dataListener.failure(proxy, data, e);
				} catch(RuntimeException e) {
					throw e;
				} finally {
					MDCUtil.setMDC(proxy.isServerSide(), proxy.getChannelId());
				}
			}
		});
	}

}
