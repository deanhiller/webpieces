package com.webpieces.httpparser.impl.data;

import java.nio.charset.Charset;

import com.webpieces.httpparser.api.DataWrapper;

public class EmptyWrapper implements DataWrapper {

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
		return "";
	}

	@Override
	public byte[] createByteArray() {
		return new byte[0];
	}

}
