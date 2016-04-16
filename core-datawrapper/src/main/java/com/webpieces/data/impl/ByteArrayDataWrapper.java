package com.webpieces.data.impl;

import java.nio.charset.Charset;

public class ByteArrayDataWrapper extends AbstractDataWrapper {

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

}
