package org.webpieces.data.api;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestReleaseBuffersToPool {
	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	private MockPool pool = new MockPool();

	private ByteBuffer create(String data) {
		return ByteBuffer.wrap(data.getBytes());
	}
	
	@Test
	public void testBaseCaseByteBufferWrapper() {
		ByteBuffer buf1 = create("0123456789");
		
		DataWrapper wrapper = dataGen.wrapByteBuffer(buf1);
		wrapper.releaseUnderlyingBuffers(pool);

		Assert.assertTrue(pool.releasedBuffers.contains(buf1));
	}
	
	@Test
	public void testChainedWrapper() {
		ByteBuffer buf1 = create("0123456789");
		ByteBuffer buf2 = create("9876543210");
		
		DataWrapper wrapper = dataGen.wrapByteBuffer(buf1);
		DataWrapper allDat = dataGen.chainDataWrappers(wrapper, buf2);
		
		allDat.releaseUnderlyingBuffers(pool);

		Assert.assertTrue(pool.releasedBuffers.contains(buf1));
		Assert.assertTrue(pool.releasedBuffers.contains(buf2));
	}
	
	@Test
	public void testSplitProxyBasic() {
		ByteBuffer buf1 = create("0123456789");
		
		DataWrapper wrapper = dataGen.wrapByteBuffer(buf1);

		List<? extends DataWrapper> split = dataGen.split(wrapper, 3);
		
		DataWrapper wrap1 = split.get(0);
		DataWrapper wrap2 = split.get(1);
		
		wrap1.releaseUnderlyingBuffers(pool);
		
		Assert.assertTrue(!pool.releasedBuffers.contains(buf1));

		wrap2.releaseUnderlyingBuffers(pool);
		
		Assert.assertTrue(pool.releasedBuffers.contains(buf1));
	}
	
	@Test
	public void testChainThenSplitBasic() {
		ByteBuffer buf1 = create("0123456789");
		ByteBuffer buf2 = create("2222222222");
		ByteBuffer buf3 = create("3333333333");
		
		DataWrapper wrap1 = dataGen.wrapByteBuffer(buf1);
		
		DataWrapper all = dataGen.chainDataWrappers(wrap1, buf2, buf3);
		
		List<? extends DataWrapper> split = dataGen.split(all, 15);
		
		DataWrapper left = split.get(0);
		DataWrapper right = split.get(1);
		
		left.releaseUnderlyingBuffers(pool);
		
		Assert.assertEquals(1, pool.releasedBuffers.size());
		Assert.assertTrue(pool.releasedBuffers.contains(buf1));
		Assert.assertTrue(!pool.releasedBuffers.contains(buf2));
		Assert.assertTrue(!pool.releasedBuffers.contains(buf3));
		
		right.releaseUnderlyingBuffers(pool);
		
		Assert.assertTrue(pool.releasedBuffers.contains(buf1));
		Assert.assertTrue(pool.releasedBuffers.contains(buf2));
		Assert.assertTrue(pool.releasedBuffers.contains(buf3));		
	}
	
}
