package org.webpieces.nio.api.mocks;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

public class MockMulithreadedSslDataListener implements DataListener {

	private List<ByteBuffer> buffers = new ArrayList<>();
	private XFuture<ByteBuffer> firstBufferFuture;
	private boolean isClosed;

	@Override
	public synchronized XFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		if(buffers.isEmpty() && firstBufferFuture != null) {
			firstBufferFuture.complete(b);
		}
		this.buffers.add(b);
		
		return XFuture.completedFuture(null);
	}

	@Override
	public void farEndClosed(Channel channel) {
		this.isClosed = true;
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
	}

	public synchronized XFuture<ByteBuffer> getFirstBuffer() {
		firstBufferFuture = new XFuture<>();
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
