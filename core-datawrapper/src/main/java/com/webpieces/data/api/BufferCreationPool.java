package com.webpieces.data.api;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class BufferCreationPool implements BufferPool {

	private static final Logger log = LoggerFactory.getLogger(BufferCreationPool.class);
	
	//a rough counter...doesn't need to be too accurate..
	private AtomicInteger counter = new AtomicInteger();
	private ConcurrentLinkedQueue<ByteBuffer> freePackets = new ConcurrentLinkedQueue<ByteBuffer>();
	private boolean isDirect;
	private int size;
	private int poolSize;
	
	public BufferCreationPool() {
		this(false, 16384, 1000);
	}
	
	public BufferCreationPool(boolean isDirect, int bufferSize, int poolSize) {
		this.isDirect = isDirect;
		this.size = bufferSize;
		this.poolSize = poolSize;
	}
	
	public ByteBuffer nextBuffer() {
		ByteBuffer buffer = freePackets.poll();

		if(buffer == null) {
			if(isDirect)
				buffer = ByteBuffer.allocateDirect(size);
			else 
				buffer = ByteBuffer.allocate(size);
		} else {
			counter.decrementAndGet();
		}
		
		return buffer;
	}

	@Override
	public void releaseBuffer(ByteBuffer buffer) {
		if(buffer.remaining() != 0) {
			throw new IllegalArgumentException("You need to consume all data from your buffer (or "
					+ "call buffer.position(buffer.limit)) to simulate consuming it though this is ill advised as you"
					+ "should be reading all your data from your buffer before releasing it");
		} if(counter.incrementAndGet() > poolSize)
			return; //we discard more than 300 buffers as we don't want to take up too much memory

		buffer.clear();
		freePackets.add(buffer);
	}

}
