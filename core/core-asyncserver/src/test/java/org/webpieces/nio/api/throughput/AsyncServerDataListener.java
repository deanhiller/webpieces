package org.webpieces.nio.api.throughput;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.webpieces.asyncserver.api.AsyncDataListener;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class AsyncServerDataListener implements AsyncDataListener {
	private static final Logger log = LoggerFactory.getLogger(AsyncServerDataListener.class);
	private BufferPool pool;
	private BytesRecorder recorder;

	public AsyncServerDataListener(BytesRecorder recorder, BufferPool pool) {
		this.recorder = recorder;
		this.pool = pool;
	}
	
	@Override
	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		log.warn("This should not be called for throughput test as we do unidirectional test");
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void connectionOpened(TCPChannel channel, boolean isReadyForWrites) {
		log.info("opened connection="+channel);
		Runnable r = new WriteRunnable(channel, pool, recorder);
		Thread t = new Thread(r);
		t.setName("NioWriterThread");
		t.start();
	}
	
	private class WriteRunnable implements Runnable {
		private TCPChannel channel;
		private BufferPool pool2;
		private BytesRecorder recorder2;
		public WriteRunnable(TCPChannel channel, BufferPool pool, BytesRecorder recorder) {
			this.channel = channel;
			pool2 = pool;
			recorder2 = recorder;
		}

		public void runImpl() throws InterruptedException, ExecutionException, TimeoutException {
			recorder2.markTime();
	        while (true) {
				ByteBuffer buffer = pool2.nextBuffer(32*1024);
				CompletableFuture<Channel> future = channel.write(buffer);
				future.get(10, TimeUnit.SECONDS);
	        }
		}

		@Override
		public void run() {
			try {
				runImpl();
			} catch (Throwable e) {
				log.error("Exception", e);
			}
		}
	}
	
	@Override
	public void farEndClosed(Channel channel) {
		log.info("far end closed="+channel);
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.info("failure on processing", e);
	}

}
