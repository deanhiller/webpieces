package org.webpieces.nio.api.throughput;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.util.time.RateRecorder;

public final class ClientDataListener implements DataListener {
	private static final Logger log = LoggerFactory.getLogger(ClientDataListener.class);
	
	private BufferPool pool2;
	private BytesRecorder recorder;
	private RateRecorder recorder2 = new RateRecorder(10, "MB/second", 1000*1000);

	public ClientDataListener(BufferPool pool2, BytesRecorder recorder) {
		this.pool2 = pool2;
		this.recorder = recorder;
	}
	
	@Override
	public CompletableFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		recorder2.increment(b.remaining());
		recorder.recordBytes(b.remaining());
		
		b.position(b.limit());
		pool2.releaseBuffer(b);
		return CompletableFuture.completedFuture(null);
	}

	@Override
	public void farEndClosed(Channel channel) {
		log.info("far end closed");
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		log.info("failure", e);
	}
	
}