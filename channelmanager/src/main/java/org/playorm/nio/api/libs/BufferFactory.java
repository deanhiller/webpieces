package org.playorm.nio.api.libs;

import java.nio.ByteBuffer;

public interface BufferFactory {

	/**
	 * Called by Threaded ChannelManager to get ByteBuffers to copy to
	 * before they are put on the queue that the threadpool reads from.
	 */
	public ByteBuffer createBuffer(Object id, int size);
	
}
