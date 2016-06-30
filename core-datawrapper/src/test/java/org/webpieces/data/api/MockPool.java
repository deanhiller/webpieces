package org.webpieces.data.api;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;

import org.webpieces.data.api.BufferPool;

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

}
