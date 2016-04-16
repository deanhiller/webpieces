package com.webpieces.httpparser.impl.data;

import java.nio.charset.Charset;

public class EmptyWrapper extends AbstractDataWrapper  {

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
		return new byte[0];
	}

}
