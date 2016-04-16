package com.webpieces.data.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class ByteBufferDataWrapper extends AbstractDataWrapper {
	
	private ByteBuffer buffer;
	
	public ByteBufferDataWrapper(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public int getReadableSize() {
		return buffer.remaining();
	}

	@Override
	public byte readByteAt(int i) {
		return buffer.get(i);
	}

	@Override
	public String createStringFrom(int offset, int length, Charset charSet) {
		//wanted to go from piece of ByteBuffer to String directly but can't find a good way...
		byte[] data = new byte[length];
		for(int i = 0; i < length; i++) {
			data[i] = readByteAt(i+offset);
		}
		
		return new String(data, charSet);
	}

	@Override
	public byte[] createByteArray() {
		byte[] data = new byte[buffer.remaining()];
		buffer.mark();
		buffer.get(data);
		buffer.reset();
		return data;
	}
}
