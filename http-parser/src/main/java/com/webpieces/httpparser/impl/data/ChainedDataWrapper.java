package com.webpieces.httpparser.impl.data;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.webpieces.httpparser.api.DataWrapper;
import com.webpieces.httpparser.api.DataWrapperGenerator;

public class ChainedDataWrapper extends AbstractDataWrapper {

	private List<DataWrapper> wrappers = new ArrayList<>();
	private DataWrapperGenerator generator;

	public ChainedDataWrapper(DataWrapper wrapper1, DataWrapper wrapper2, DataWrapperGenerator generator) {
		wrappers.add(wrapper1);
		wrappers.add(wrapper2);
		this.generator = generator;
	}

	public ChainedDataWrapper(List<DataWrapper> wrappers, DataWrapperGenerator generator) {
		this.wrappers = wrappers;
		this.generator = generator;
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
		if(length == 0)
			return "";
		
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

	public List<DataWrapper> split(int splitAtPosition) {
		List<DataWrapper> wrappersInBegin = new ArrayList<>();
		List<DataWrapper> wrappersInEnd = new ArrayList<>();
		
		boolean foundSplit = false;
		List<DataWrapper> splitBuffers = null;
		for(DataWrapper wrapper : wrappers) {
			if (!foundSplit) {
				if(splitAtPosition == wrapper.getReadableSize()) {
					wrappersInBegin.add(wrapper);
					foundSplit = true;
				} else if(splitAtPosition < wrapper.getReadableSize()) {
					splitBuffers = generator.split(wrapper, splitAtPosition);
					foundSplit = true;
				} else {
					wrappersInBegin.add(wrapper);
					splitAtPosition = splitAtPosition - wrapper.getReadableSize();	
				}
			} else {
				wrappersInEnd.add(wrapper);
			}
		}

		if(splitBuffers != null) {
			wrappersInBegin.add(splitBuffers.get(0));
			wrappersInEnd.add(0, splitBuffers.get(1));
		}

		DataWrapper wrapper1;
		if(wrappersInBegin.size() > 0) 
			wrapper1 = new ChainedDataWrapper(wrappersInBegin, generator);
		else 
			wrapper1 = new EmptyWrapper();
		
		DataWrapper wrapper2;
		if(wrappersInEnd.size() > 0) 
			wrapper2 = new ChainedDataWrapper(wrappersInEnd, generator);
		else
			wrapper2 = new EmptyWrapper();
		
		List<DataWrapper> finalTwo = new ArrayList<>();
		finalTwo.add(wrapper1);
		finalTwo.add(wrapper2);
		return finalTwo;
	}

}
