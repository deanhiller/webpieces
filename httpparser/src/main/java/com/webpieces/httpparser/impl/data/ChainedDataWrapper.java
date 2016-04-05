package com.webpieces.httpparser.impl.data;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.webpieces.httpparser.api.DataWrapper;

public class ChainedDataWrapper extends AbstractDataWrapper {

	private List<DataWrapper> wrappers = new ArrayList<>();

	public ChainedDataWrapper(DataWrapper wrapper1, DataWrapper wrapper2) {
		wrappers.add(wrapper1);
		wrappers.add(wrapper2);
	}
	
	@Override
	public int getReadableSize() {
		int size = 0;
		for(DataWrapper wrapper : wrappers) {
			size += wrapper.getReadableSize();
		}
		return size;
	}

	@Override
	public byte readByteAt(int i) {
		for(DataWrapper wrapper : wrappers) {
			int size = wrapper.getReadableSize();
			if(i < size) {
				return wrapper.readByteAt(i);
			}
			i = i - size;
		}
		
		throw new IndexOutOfBoundsException("i="+i+" is out of bounds of size="+getReadableSize());
	}

	@Override
	public String createStringFrom(int initialOffset, int length, Charset charSet) {
		String result = "";
		int lengthLeftToRead = length;
		int offset = initialOffset;
		for(DataWrapper wrapper : wrappers) {
			int size = wrapper.getReadableSize();
			if(offset < size) {
				if(offset + lengthLeftToRead <= size) {
					result += wrapper.createStringFrom(offset, lengthLeftToRead, charSet);
					return result;
				}
				int leftInBuffer = size - offset;
				result += wrapper.createStringFrom(offset, leftInBuffer, charSet);
				offset = 0; //since we read in this data, offset for next datawrapper is 0
				lengthLeftToRead -= leftInBuffer;
			} else {
				offset -= size;
			}
		}
		
		throw new IndexOutOfBoundsException("offset="+offset+" length="+length+" is larger than size="+getReadableSize());
	}

	@Override
	public byte[] createByteArray() {
		byte[] copy = new byte[getReadableSize()];
		
		int offset = 0;
		for(DataWrapper wrapper : wrappers) {
			byte[] data = wrapper.createByteArray();
			int size = wrapper.getReadableSize();
			System.arraycopy(data, 0, copy, offset, size);
			offset += size;
		}
		
		return copy;
	}

	public void addMoreData(DataWrapper secondData) {
		wrappers.add(secondData);
	}

	@Override
	public int getNumLayers() {
		int max = 0;
		for(DataWrapper wrapper: wrappers) {
			int num = wrapper.getNumLayers();
			if(num > max) 
				max = num;
		}
		return max+1;
	}

}
