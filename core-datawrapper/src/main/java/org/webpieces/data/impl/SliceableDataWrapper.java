package org.webpieces.data.impl;

import java.nio.ByteBuffer;

import org.webpieces.data.api.BufferPool;

public abstract class SliceableDataWrapper extends AbstractDataWrapper {

	private int refCount = 1;

	public abstract ByteBuffer getSlicedBuffer(int offset, int length);

	public void increaseRefCount() {
		refCount++;
	}
	
	@Override
	public void releaseUnderlyingBuffers(BufferPool pool) {
		refCount--;
		if(refCount <= 0)
			releaseImpl(pool);
	}

	protected abstract void releaseImpl(BufferPool pool);
	
}
