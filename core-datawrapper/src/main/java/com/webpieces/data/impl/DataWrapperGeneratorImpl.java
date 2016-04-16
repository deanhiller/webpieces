package com.webpieces.data.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.webpieces.data.api.DataWrapper;
import com.webpieces.data.api.DataWrapperGenerator;

public class DataWrapperGeneratorImpl implements DataWrapperGenerator {

	@Override
	public DataWrapper wrapByteArray(byte[] data) {
		return new ByteArrayDataWrapper(data);
	}

	@Override
	public DataWrapper wrapByteBuffer(ByteBuffer buffer) {
		if(buffer.position() != 0)
			throw new IllegalArgumentException("You probably forgot to call buffer.flip() so the buffer is made readable after writing to it.  position must be 0");
		else if(!buffer.hasRemaining())
			throw new IllegalArgumentException("There is no data in this buffer.");
		
		return new ByteBufferDataWrapper(buffer);
	}

	@Override
	public DataWrapper chainDataWrappers(DataWrapper firstData, DataWrapper secondData) {
		if(firstData instanceof EmptyWrapper) {
			return secondData;
		} else if(secondData instanceof EmptyWrapper) {
			return firstData;
		} else if(firstData instanceof ChainedDataWrapper) {
			ChainedDataWrapper chained = (ChainedDataWrapper) firstData;
			chained.addMoreData(secondData);
			return chained;
		} else if(secondData instanceof ChainedDataWrapper) {
			ChainedDataWrapper chained = (ChainedDataWrapper) secondData;
			chained.addMoreData(firstData);
			return chained;
		}
		return new ChainedDataWrapper(firstData, secondData, this);
	}

	@Override
	public DataWrapper emptyWrapper() {
		return new EmptyWrapper();
	}

	@Override
	public List<DataWrapper> split(DataWrapper dataToRead, int splitAtPosition) {
		if(dataToRead instanceof ChainedDataWrapper) {
			//A split proxy should never have a reference to a chained one or there is the potential for
			//a memory leak in that as you grow, the right side is not releasing data from ChainedDataWrapper and you end
			//up with  byteWrapper <-chained <- split <-chained <-split <- chained.... and it keeps going as data
			//comes in never releasing the first set of data
			return splitChainedWrapper((ChainedDataWrapper) dataToRead, splitAtPosition);
		}
		
		List<DataWrapper> tuple = new ArrayList<>();
		if(splitAtPosition > dataToRead.getReadableSize()) {
			throw new IllegalArgumentException("splitPosition="+splitAtPosition+" is greater than size of data="+dataToRead.getReadableSize());
		}
		
		SplitProxyWrapper wrapper1 = new SplitProxyWrapper(dataToRead, 0, splitAtPosition);
		
		DataWrapper wrapper2;
		if(dataToRead.getReadableSize() - splitAtPosition == 0) {
			wrapper2 = new EmptyWrapper();
		} else {
			wrapper2 = 
				new SplitProxyWrapper(dataToRead, splitAtPosition, dataToRead.getReadableSize() - splitAtPosition);
		}
		
		tuple.add(wrapper1);
		tuple.add(wrapper2);
		
		return tuple;
	}

	private List<DataWrapper> splitChainedWrapper(ChainedDataWrapper dataToRead, int splitAtPosition) {
		return dataToRead.split(splitAtPosition);
	}
}
