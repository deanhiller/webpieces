package com.webpieces.httpparser.api;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.junit.Assert;
import org.junit.Test;

import com.webpieces.data.api.DataWrapper;
import com.webpieces.data.api.DataWrapperGenerator;
import com.webpieces.data.api.DataWrapperGeneratorFactory;

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
	
}
