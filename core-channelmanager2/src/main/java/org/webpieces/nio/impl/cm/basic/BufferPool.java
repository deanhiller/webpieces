package org.webpieces.nio.impl.cm.basic;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.webpieces.nio.impl.util.BufferListener;

public class BufferPool implements BufferListener {

	public Set<ByteBuffer> freePackets = new HashSet<ByteBuffer>();
	
	public synchronized ByteBuffer nextBuffer() {
		ByteBuffer b = null;
		Iterator<ByteBuffer> iter = freePackets.iterator();
		if(iter.hasNext()) {
			b = iter.next();
			iter.remove();
		} else
			b = ByteBuffer.allocateDirect(1000);
		
		return b;
	}

	public synchronized void releaseBuffer(ByteBuffer buffer) {
		if(freePackets.size() > 300)
			return; //we discard more than 300 buffers as we don't want to take up too much memory
		freePackets.add(buffer);
	}

}
