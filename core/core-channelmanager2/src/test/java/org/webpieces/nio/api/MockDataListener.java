package org.webpieces.nio.api;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class MockDataListener implements DataListener {

	private List<ByteBuffer> buffers = new ArrayList<>();
	private CompletableFuture<ByteBuffer> firstBufferFuture;
	private boolean isClosed;

	@Override
	public synchronized void incomingData(Channel channel, ByteBuffer b, boolean isOpeningConnection) {
		if(buffers.isEmpty() && firstBufferFuture != null) {
			firstBufferFuture.complete(b);
		}
		this.buffers.add(b);
	}

	@Override
	public void farEndClosed(Channel channel) {
		this.isClosed = true;
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
	}

	@Override
	public void applyBackPressure(Channel channel) {
	}

	@Override
	public void releaseBackPressure(Channel channel) {
	}

	public synchronized CompletableFuture<ByteBuffer> getFirstBuffer() {
		firstBufferFuture = new CompletableFuture<>();
		if(buffers.isEmpty()) {
			return firstBufferFuture;
		}
		firstBufferFuture.complete(buffers.get(0));
		return firstBufferFuture;
	}

	public boolean isClosed() {
		return isClosed;
	}

}
