package org.webpieces.data.impl;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.BufferWebManaged;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.util.metrics.MetricsCreator;

/**
 * Feel free to completely override this class but basically as ChannelManager feeds
 * ByteBuffers, they are 100% ALWAYS created from this pool.  The SSL layer if one exists will
 * release them OR the http1 OR http2 layer will release them as processing happens.  IF you
 * use ChannelManager directly, you can release them for performance enhancement so you don't need
 * to constantly GC ByteBuffers.  You do NOT need to release them, and if you don't release,
 * the pool gets exhausted and degrades to just a ByteBuffer creation tool only.
 *
 * In most cases, 5k bytes is reasonable.  In fact, SSLEngine ALWAYS spits out around 1389 unencrypted
 * bytes when decrypting BUT forces a 17k ByteBuffer(what a waste!!!) so it's VERY important for
 * those Buffers to be released when consuming plain decrypted packets so you don't allocate 17k, then
 * deallocate 17k.  Instead, just consume from the SSLPool.
 * 
 * We could behind the BufferPool have N TwoPoolss as well to further reduce
 * contention if any existed since this class is shared between async http parser,
 * async ssl engine, clients, channelmanager writes involving all threads from
 * SessionExecutor and all threads that call write() from the client as well...DONE ---> TwoPools.java
 * 
 * @author dhiller
 */
public class BufferCreationPool implements BufferPool, BufferWebManaged {

	private static final Logger log = LoggerFactory.getLogger(BufferCreationPool.class);

	public static final int DEFAULT_MAX_BUFFER_SIZE = 16_665+2048;
	
	//a rough counter...doesn't need to be too accurate..
	private AtomicInteger counter = new AtomicInteger();
	private ConcurrentLinkedQueue<ByteBuffer> freePackets = new ConcurrentLinkedQueue<ByteBuffer>();
	private boolean isDirect;
	private int bufferSize;
	private int poolSize = 2000;
	private Counter checkoutCounter;
	private Counter checkinCounter;
	
    @Inject
    public BufferCreationPool(MeterRegistry metrics) {
    	this("webpieces.bufPool", metrics);
    }
    
	public BufferCreationPool(String id, MeterRegistry metrics) {
		this(id, metrics, false, DEFAULT_MAX_BUFFER_SIZE);
	}

	/**
	 * @deprecated Use the constructor we call instad of this one
	 */
	@Deprecated
	public BufferCreationPool(boolean isDirect, int bufferSize) {
		this("", new SimpleMeterRegistry(), isDirect, bufferSize);
	}

	public BufferCreationPool(String id, MeterRegistry metrics, boolean isDirect, int bufferSize) {
		this.isDirect = isDirect;
		this.bufferSize = bufferSize;

		MetricsCreator.createGauge(metrics, id, freePackets, (q) -> q.size());
		checkoutCounter = MetricsCreator.createCounter(metrics, id, "checkout", false);
		checkinCounter = MetricsCreator.createCounter(metrics, id, "checkin", false);
	}
	
	public ByteBuffer nextBuffer(int minSize) {
		checkoutCounter.increment();
		
		if(bufferSize < minSize) {
			log.warn("minSize="+minSize+" requests is larger than the buffer size provided by this pool="+bufferSize+".  You should reconfigure this if it happens tooo much to speed things up.  this can also cause issue with backpressure configuration");
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

	public ByteBuffer createWithDataWrapper(DataWrapper data) {
		ByteBuffer byteBuffer = nextBuffer(data.getReadableSize());
		byteBuffer.put(data.createByteArray());
		byteBuffer.flip();

		return byteBuffer;
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
		} 

		checkinCounter.increment();

		if(counter.incrementAndGet() > poolSize)
			return; //we discard more than N buffers as we don't want to take up too much memory
		else if(buffer.capacity() != bufferSize) {
			return; //discard buffers that are released and are smaller or bigger
		}
		buffer.clear();
		freePackets.add(buffer);
	}

	public int getSupportedBufferSize() {
		return bufferSize;
	}

	@Override
	public void setBufferPoolSize(int size) {
		this.poolSize = size;
	}

	@Override
	public int getBufferPoolSize() {
		return poolSize;
	}

	@Override
	public String getCategory() {
		return "Webpieces Core";
	}

	@Override
	public int getSuggestedBufferSize() {
		return bufferSize;
	}

}
