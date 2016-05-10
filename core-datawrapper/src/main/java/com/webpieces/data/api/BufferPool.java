package com.webpieces.data.api;

import java.nio.ByteBuffer;

public interface BufferPool {

	/**
	 * Either create a buffer or return one from the pool.
	 * 
	 * @return
	 */
	public ByteBuffer nextBuffer(int minSize);
		
	/**
	 * Allow releasing ByteBuffer's back to a pool to be re-used since copying bytes around is expensive and 
	 * this can help with garbage collection on large byte structures
	 * 
	 * @param buffer
	 */
	public void releaseBuffer(ByteBuffer buffer);
}
