package com.webpieces.data.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

public class ByteArrayDataWrapper extends SliceableDataWrapper {

	private byte[] data;

	public ByteArrayDataWrapper(byte[] data) {
		this.data = data;
	}
	
	@Override
	public int getReadableSize() {
		return data.length;
	}

	@Override
	public byte readByteAt(int i) {
		return data[i];
	}

	@Override
	public String createStringFrom(int offset, int length, Charset charSet) {
		return new String(data, offset, length, charSet);
	}

	@Override
	public byte[] createByteArray() {
		return data;
	}

	@Override
	public void addUnderlyingBuffersToList(List<ByteBuffer> buffers) {
		buffers.add(ByteBuffer.wrap(data));
	}

	@Override
	public ByteBuffer getSlicedBuffer(int offset, int length) {
		return ByteBuffer.wrap(data, offset, length);
	}

}
