package com.webpieces.data.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import com.webpieces.data.api.BufferPool;
import com.webpieces.data.api.DataWrapper;

public class ChainedDataWrapper extends AbstractDataWrapper {

	private List<SliceableDataWrapper> wrappers = new ArrayList<>();

	public ChainedDataWrapper(SliceableDataWrapper wrapper1, SliceableDataWrapper wrapper2) {
		wrappers.add(wrapper1);
		wrappers.add(wrapper2);
	}

	public ChainedDataWrapper(List<SliceableDataWrapper> wrappers) {
		this.wrappers = wrappers;
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
			@SuppressWarnings("deprecation")
			byte[] data = wrapper.createByteArray();
			int size = wrapper.getReadableSize();
			System.arraycopy(data, 0, copy, offset, size);
			offset += size;
		}
		
		return copy;
	}

	public void addMoreData(DataWrapper secondData) {
		if(secondData instanceof ChainedDataWrapper) {
			ChainedDataWrapper wrap = (ChainedDataWrapper) secondData;
			wrappers.addAll(wrap.getAllWrappers());
			return;
		} else if(!(secondData instanceof SliceableDataWrapper)) {
			throw new IllegalArgumentException("Only SliceableDataWrappers or ChainedDataWrappers are allowed to be chained");
		}
		
		SliceableDataWrapper wrap = (SliceableDataWrapper) secondData;
		wrappers.add(wrap);
	}

	private List<SliceableDataWrapper> getAllWrappers() {
		return wrappers;
	}

	@Override
	public int getNumLayers() {
		int max = 0;
		for(DataWrapper wrapper: wrappers) {
			@SuppressWarnings("deprecation")
			int num = wrapper.getNumLayers();
			if(num > max) 
				max = num;
		}
		return max+1;
	}

	@Override
	public void addUnderlyingBuffersToList(List<ByteBuffer> buffers) {
		for(SliceableDataWrapper wrapper : wrappers) {
			wrapper.addUnderlyingBuffersToList(buffers);
		}
	}

	public List<SliceableDataWrapper> getWrappers() {
		return wrappers;
	}

	@Override
	public void releaseUnderlyingBuffers(BufferPool pool) {
		for(DataWrapper wrapper : wrappers) {
			wrapper.releaseUnderlyingBuffers(pool);
		}
	}

}
