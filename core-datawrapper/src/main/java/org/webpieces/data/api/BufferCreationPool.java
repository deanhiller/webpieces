package org.webpieces.data.api;

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
 * We could behind the BufferPool have N BufferCreationPools as well to further reduce
 * contention if any existed since this class is shared between async http parser,
 * async ssl engine, clients, channelmanager writes involving all threads from
 * SessionExecutor and all threads that call write() from the client as well...TBD
 * 
 * @author dhiller
 */
public class BufferCreationPool implements BufferPool {

	private static final Logger log = LoggerFactory.getLogger(BufferCreationPool.class);
	
	//a rough counter...doesn't need to be too accurate..
	private AtomicInteger counter = new AtomicInteger();
	private ConcurrentLinkedQueue<ByteBuffer> freePackets = new ConcurrentLinkedQueue<ByteBuffer>();
	private boolean isDirect;
	private int bufferSize;
	private int poolSize;
	
	public BufferCreationPool() {
		this(false, 16921, 1000);
	}
	
	public BufferCreationPool(boolean isDirect, int bufferSize, int poolSize) {
		this.isDirect = isDirect;
		this.bufferSize = bufferSize;
		this.poolSize = poolSize;
	}
	
	public ByteBuffer nextBuffer(int minSize) {
		if(bufferSize < minSize) {
			log.error("minSize="+minSize+" requests is larger than the buffer size provided by this pool="+bufferSize+".  You should reconfigure this ");
			return createBuffer(minSize);
		}
		
		ByteBuffer buffer = freePackets.poll();

		if(buffer == null) {
			buffer = createBuffer(bufferSize);
		} else {
			counter.decrementAndGet();
		}
		
		return buffer;
	}

	private ByteBuffer createBuffer(int size2) {
		ByteBuffer buffer;
		if(isDirect)
			buffer = ByteBuffer.allocateDirect(size2);
		else 
			buffer = ByteBuffer.allocate(size2);
		return buffer;
	}

	@Override
	public void releaseBuffer(ByteBuffer buffer) {
		if(buffer.remaining() != 0) {
			throw new IllegalArgumentException("You need to consume all data from your buffer (or "
					+ "call buffer.position(buffer.limit)) to simulate consuming it though this is ill advised as you"
					+ "should be reading all your data from your buffer before releasing it");
		} if(counter.incrementAndGet() > poolSize)
			return; //we discard more than N buffers as we don't want to take up too much memory
		else if(buffer.capacity() < bufferSize) {
			return; //discard buffers that are released and are smaller
		}
		buffer.clear();
		freePackets.add(buffer);
	}

}
