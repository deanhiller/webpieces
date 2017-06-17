package org.webpieces.data.api;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestByteBufferDataWrapper {

	private DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();
	
	@Test
	public void testBasicByteBuffer() {
		byte[] data = "0123456789".getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(data);
		DataWrapper wrapper = dataGen.wrapByteBuffer(buffer);

		String str1 = wrapper.createStringFrom(0, wrapper.getReadableSize(), Charset.defaultCharset());
		Assert.assertEquals("0123456789", str1);
	}
	
	@Test
	public void testPartialBuffer() {
		byte[] data = "0123456789".getBytes();
		ByteBuffer buffer = ByteBuffer.allocate(30);
		buffer.put(data);
		buffer.flip();
		DataWrapper wrapper = dataGen.wrapByteBuffer(buffer);

		String str1 = wrapper.createStringFrom(0, wrapper.getReadableSize(), Charset.defaultCharset());
		Assert.assertEquals("0123456789", str1);
	}
	
	@Test
	public void testMultipleSlicing() {
		byte[] data = "0123456789987654321".getBytes();
		ByteBuffer buffer = ByteBuffer.wrap(data);
		DataWrapper wrapper = dataGen.wrapByteBuffer(buffer);
		
		List<? extends DataWrapper> split = dataGen.split(wrapper, 10);
		DataWrapper first = split.get(0);
		
		List<? extends DataWrapper> split2 = dataGen.split(first, 5);
		DataWrapper veryFirst = split2.get(0);
		DataWrapper secondOfFirst = split2.get(1);
		
		Assert.assertEquals(2, veryFirst.getNumLayers());
		
		String firstStr = veryFirst.createStringFrom(0, veryFirst.getReadableSize(), Charset.defaultCharset());
		Assert.assertEquals("01234", firstStr);
		String secondStr = secondOfFirst.createStringFrom(0, secondOfFirst.getReadableSize(), Charset.defaultCharset());
		Assert.assertEquals("56789", secondStr);
		
		List<? extends DataWrapper> split3 = dataGen.split(secondOfFirst, 2);
		DataWrapper theOne = split3.get(0);
		
		String theOneStr = theOne.createStringFrom(0, theOne.getReadableSize(), Charset.defaultCharset());
		Assert.assertEquals("56", theOneStr);
		
		List<ByteBuffer> buffers = new ArrayList<>(); 
		theOne.addUnderlyingBuffersToList(buffers);
		
		Assert.assertEquals(1, buffers.size());
		
		ByteBuffer buf = buffers.get(0);
		Assert.assertEquals(2, buf.remaining());
		byte[] bytes = new byte[2];
		buf.get(bytes);
		
		String result = new String(bytes);
		Assert.assertEquals("56", result);
	}
	
}
