package org.webpieces.data.api;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

public class MockPool implements BufferPool {

	public Set<ByteBuffer> releasedBuffers = new HashSet<>();
	
	@Override
	public void releaseBuffer(ByteBuffer buffer) {
		releasedBuffers.add(buffer);
	}

	@Override
	public ByteBuffer nextBuffer(int minSize) {
		return null;
	}

	@Override
	public ByteBuffer createWithDataWrapper(DataWrapper data) {
		return null;
	}

	@Override
	public int getSuggestedBufferSize() {
		return 5000;
	}

}
