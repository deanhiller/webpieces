package org.webpieces.data.api;

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

	/** Create a ByteBuffer filled with the DataWrapper */
	public ByteBuffer createWithDataWrapper(DataWrapper data);

	/**
	 * Gets the suggested size.  This is the size of the base buffer for TwoPools.java 
	 * or otherwise the suggested size so parsers using this pool can ask what size to 
	 * demarcate chunks of data to feed to optimize for pool use 
	 */
	public int getSuggestedBufferSize();
	
}
