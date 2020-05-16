package org.webpieces.data.api;

import io.micrometer.core.instrument.MeterRegistry;

import org.webpieces.data.impl.BufferCreationPool;

import java.nio.ByteBuffer;

import javax.inject.Inject;

/**
 * Feel free to completely override this class but basically as ChannelManager feeds
 * ByteBuffers, they are 100% ALWAYS created from one of two pools.  The SSL layer if one exists will
 * release them OR the http1 OR http2 layer will release them as processing happens.  IF you
 * use ChannelManager directly, you can release them for performance enhancement so you don't need
 * to constantly GC ByteBuffers.  You do NOT need to release them, and if you don't release,
 * the pool gets exhausted and degrades to just a ByteBuffer creation tool only.
 *
 * In most cases, 5k bytes is reasonable.  In fact, SSLEngine ALWAYS spits out around 1389 unencrypted
 * bytes when decrypting BUT forces a 17k ByteBuffer(what a waste!!!) or will BUFFER_OVERFLOW EVEN though
 * it only writes 1389 bytes.  (ie. it didn't seem to need a 17k buffer BUT perhaps needs it that size to
 * do it's own work using the buffer temporarily)  so it's VERY important for
 * those Buffers to be released when consuming plain decrypted packets so you don't allocate 17k, then
 * deallocate 17k.  Instead, just consume from the SSLPool.
 */
public class TwoPools implements BufferPool, TwoPoolManaged {

	public static final int DEFAULT_MAX_BASE_BUFFER_SIZE = 16_665+2048;
	
    // Add 1024 as a room for compressed data and another 1024 for Apache Harmony compatibility.
	public static final int DEFAULT_MAX_SSL_BUFFER_SIZE = 2*DEFAULT_MAX_BASE_BUFFER_SIZE; 

    private final BufferCreationPool smallPool;
    private final BufferCreationPool sslPool;

    @Inject
    public TwoPools(MeterRegistry metrics) {
    	this("webpieces.bufPool", metrics);
    }
    
    public TwoPools(String id, MeterRegistry metrics) {
        this(id, metrics, false, DEFAULT_MAX_BASE_BUFFER_SIZE, DEFAULT_MAX_SSL_BUFFER_SIZE);
    }

    public TwoPools(String id, MeterRegistry metrics, boolean isDirect, int baseBufMaxSize, int sslBufMaxSize) {
        smallPool = new BufferCreationPool(id+".small", metrics, isDirect, baseBufMaxSize);
        sslPool = new BufferCreationPool(id+".small", metrics, isDirect, sslBufMaxSize);
    }

        @Override
    public ByteBuffer nextBuffer(int minSize) {
        if(minSize > smallPool.getSupportedBufferSize()) {
            return sslPool.nextBuffer(minSize);
        } else {
            return smallPool.nextBuffer(minSize);
        }
    }

    @Override
    public void releaseBuffer(ByteBuffer buffer) {
        if(buffer.capacity() > smallPool.getSupportedBufferSize()) {
            sslPool.releaseBuffer(buffer);
        } else {
            smallPool.releaseBuffer(buffer);
        }
    }

    @Override
    public ByteBuffer createWithDataWrapper(DataWrapper data) {
        if(data.getReadableSize() > smallPool.getSupportedBufferSize()) {
            return sslPool.createWithDataWrapper(data);
        } else {
            return smallPool.createWithDataWrapper(data);
        }
    }

    @Override
    public String getCategory() {
        return "Webpieces Core";
    }

    @Override
    public void setBaseBufferPoolSize(int size) {
        smallPool.setBufferPoolSize(size);
    }

    @Override
    public int getBaseBufferPoolSize() {
        return smallPool.getBufferPoolSize();
    }

    @Override
    public void setSslBufferPoolSize(int size) {
        sslPool.setBufferPoolSize(size);
    }

    @Override
    public int getSslBufferPoolSize() {
        return sslPool.getBufferPoolSize();
    }

	@Override
	public int getSuggestedBufferSize() {
		return smallPool.getBufferPoolSize();
	}
}
