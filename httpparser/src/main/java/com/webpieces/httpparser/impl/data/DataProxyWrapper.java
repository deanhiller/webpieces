package com.webpieces.httpparser.impl.data;

import java.nio.charset.Charset;

import com.webpieces.httpparser.api.DataWrapper;

public class DataProxyWrapper implements DataWrapper {

	private DataWrapper wrapper;
	private int offset;
	private int length;

	public DataProxyWrapper(DataWrapper wrapper, int offset, int length) {
		this.wrapper = wrapper;
		this.offset = offset;
		this.length = length;
	}
	
	@Override
	public int getReadableSize() {
		return length;
	}

	@Override
	public byte readByteAt(int i) {
		return wrapper.readByteAt(offset + i);
	}

	@Override
	public String createStringFrom(int offset, int length, Charset charSet) {
		int endMarkOfProxyView = this.offset +  this.length;
		int endMarkOfRequest = this.offset + offset + length;
		if(offset > endMarkOfProxyView) {
			throw new IndexOutOfBoundsException("offset="+offset+" is outside the bounds of this view, endOfView="
						+endMarkOfProxyView+" view.offset="+this.offset+" view.length="+this.length);
		} else if(endMarkOfRequest > endMarkOfProxyView) {
			throw new IndexOutOfBoundsException("this.offset="+offset+" this.length="+length
					+" request goes outside of view.  view end="+endMarkOfProxyView+
					".  request.offset="+offset+" request.length="+length);
		}
		int newOffset = offset+this.offset;
		return wrapper.createStringFrom(newOffset, length, charSet);
	}

	@Override
	public byte[] createByteArray() {
		byte[] copy = new byte[length];
		for(int i = offset; i < copy.length; i++) {
			copy[i] = readByteAt(i);
		}
		return copy;
	}
}
