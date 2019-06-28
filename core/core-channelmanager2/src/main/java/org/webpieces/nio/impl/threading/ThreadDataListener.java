package org.webpieces.nio.impl.threading;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.slf4j.MDC;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
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
	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		CompletableFuture<Void> future = new CompletableFuture<Void>();
		executor.execute(channel, new Runnable() {
			@Override
			public void run() {
				MDC.put("socket", ""+channel);
				try {
					
					CompletableFuture<Void> fut = dataListener.incomingData(channel, b);
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
					MDC.clear();
				}
				


			}
		});
		
		return future;
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

}
