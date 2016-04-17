package org.webpieces.nio.api;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

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

	public Set<ByteBuffer> freePackets = new HashSet<ByteBuffer>();
	private boolean isDirect;
	private int size;
	
	public BufferCreationPool(boolean isDirect, int size) {
		this.isDirect = isDirect;
		this.size = size;
	}
	
	public synchronized ByteBuffer nextBuffer() {
		ByteBuffer b = null;
		Iterator<ByteBuffer> iter = freePackets.iterator();
		if(iter.hasNext()) {
			b = iter.next();
			iter.remove();
		} else if(isDirect) {
			b = ByteBuffer.allocateDirect(size);
		} else 
			b = ByteBuffer.allocate(size);
		return b;
	}

	public synchronized void releaseBuffer(ByteBuffer buffer) {
		if(freePackets.size() > 300)
			return; //we discard more than 300 buffers as we don't want to take up too much memory
		freePackets.add(buffer);
	}

}
