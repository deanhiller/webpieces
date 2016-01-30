package com.webpieces.httpparser.impl.data;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import com.webpieces.httpparser.api.DataWrapper;

public class ByteBufferDataWrapper implements DataWrapper {
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
		throw new UnsupportedOperationException("not done yet");
	}

	@Override
	public String createStringFrom(int offset, int length, Charset charSet) {
		throw new UnsupportedOperationException("not done yet");
	}

	@Override
	public byte[] createByteArray() {
		throw new UnsupportedOperationException("not done yet");
	}
}
