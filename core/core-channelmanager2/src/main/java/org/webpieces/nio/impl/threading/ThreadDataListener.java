package org.webpieces.nio.impl.threading;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
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
	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		executor.execute(proxy, new DataListeneRunanble(dataListener, proxy, b, future));
		
		return future;
	}

	private static class DataListeneRunanble implements Runnable {
		private DataListener dataListener;
		private ThreadChannel proxy;
		private ByteBuffer buffer;
		private CompletableFuture<Void> future;

		public DataListeneRunanble(DataListener dataListener, ThreadChannel proxyChannel, ByteBuffer b,
				CompletableFuture<Void> future) {
					this.dataListener = dataListener;
					this.proxy = proxyChannel;
					this.buffer = b;
					this.future = future;
		}

		@Override
		public void run() {
			MDC.put("socket", ""+proxy);
			try {
				CompletableFuture<Void> fut = dataListener.incomingData(proxy, buffer);
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
				MDC.put("socket", null);
			}
		}
	}
	
	@Override
	public void farEndClosed(Channel channel) {
		executor.execute(proxy, new Runnable() {
			@Override
			public void run() {
				MDC.put("socket", ""+proxy);
				try {
					dataListener.farEndClosed(proxy);
				} catch(RuntimeException e) {
					throw e;
				} finally {
					MDC.put("socket", null);
				}
			}
		});
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		executor.execute(proxy, new Runnable() {
			@Override
			public void run() {
				MDC.put("socket", ""+proxy);
				try {
					dataListener.failure(proxy, data, e);
				} catch(RuntimeException e) {
					throw e;
				} finally {
					MDC.put("socket", null);
				}
			}
		});
	}

}
