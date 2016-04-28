package com.webpieces.data.api;

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
	public ByteBuffer nextBuffer() {
		return null;
	}

}
