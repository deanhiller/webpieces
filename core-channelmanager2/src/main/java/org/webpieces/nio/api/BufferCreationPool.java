package org.webpieces.nio.api;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Feel free to completely override this class but basically as ChannelManager feeds
 * you ByteBuffers, they are 100% ALWAYS created from this pool.  You can then release
 * the ByteBuffer to be re-used (rather than having to garbage collect request memory
 * over and over taking that performance hit).   Just call 
 * 
 * BufferCreationPool.releaseBuffer(buffer); and ChannelManager will clear that buffer 
 * and re-use it for the next set of data coming in.
 * 
 * @author dhiller
 */
public class BufferCreationPool {

	public ConcurrentLinkedQueue<ByteBuffer> freePackets = new ConcurrentLinkedQueue<ByteBuffer>();
	private boolean isDirect;
	private int size;
	
	public BufferCreationPool(boolean isDirect, int size) {
		this.isDirect = isDirect;
		this.size = size;
	}
	
	public ByteBuffer nextBuffer() {
		ByteBuffer buffer = freePackets.poll();

		if(buffer == null) {
			if(isDirect)
				buffer = ByteBuffer.allocateDirect(size);
			else 
				buffer = ByteBuffer.allocate(size);
		}
		return buffer;
	}

	public void releaseBuffer(ByteBuffer buffer) {
		if(buffer.remaining() != 0) {
			throw new IllegalArgumentException("You need to consume all data from your buffer (or "
					+ "call buffer.position(buffer.limit)) to simulate consuming it though this is ill advised as you"
					+ "should be reading all your data from your buffer before releasing it");
		} if(freePackets.size() > 300)
			return; //we discard more than 300 buffers as we don't want to take up too much memory

		buffer.clear();
		freePackets.add(buffer);
	}

}
