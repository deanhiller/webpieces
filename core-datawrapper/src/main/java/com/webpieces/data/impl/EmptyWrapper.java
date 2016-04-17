package com.webpieces.data.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

public class EmptyWrapper extends SliceableDataWrapper  {
	private static final byte[] EMPTY = new byte[0];
	private static final ByteBuffer EMPTY_BUFFER = ByteBuffer.wrap(EMPTY);

	@Override
	public int getReadableSize() {
		return 0;
	}

	@Override
	public byte readByteAt(int i) {
		throw new IndexOutOfBoundsException("Size is 0, so reading at any byte in this wrapper will throw");
	}

	@Override
	public String createStringFrom(int offset, int length, Charset charSet) {
		if(length > 0)
			throw new IllegalArgumentException("There is no data left to read.  offset="+offset+" len="+length);
		return "";
	}

	@Override
	public byte[] createByteArray() {
		return EMPTY;
	}

	@Override
	public void addUnderlyingBuffersToList(List<ByteBuffer> buffers) {
	}

	@Override
	public ByteBuffer getSlicedBuffer(int offset, int length) {
		return EMPTY_BUFFER;
	}

}
