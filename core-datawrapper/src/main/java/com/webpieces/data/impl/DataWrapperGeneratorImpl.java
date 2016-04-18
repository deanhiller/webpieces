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
			//ordering matters a ton.  If we really wanted to support this odd situation, we would have to
			//insert the firstData into the beginning of the ChainedDataWrapper but in reality, it is a client bug
			//were they are accidentally not paying attention so let's flag it instead
			throw new IllegalArgumentException("The second argument should never be a ChainedDataWrapper...you probably screwed up your ordering.  This is fail fast so you find out immediately");
		} else if(!(firstData instanceof SliceableDataWrapper)) {
			throw new IllegalArgumentException("Only SliceableDataWrappers or ChainedDataWrappers are allowed to be chained");
		} else if(!(secondData instanceof SliceableDataWrapper)) {
			throw new IllegalArgumentException("Only SliceableDataWrappers or ChainedDataWrappers are allowed to be chained");
		}
		
		SliceableDataWrapper first = (SliceableDataWrapper) firstData;
		SliceableDataWrapper second = (SliceableDataWrapper) secondData;

		return new ChainedDataWrapper(first, second, this);
	}

	@Override
	public DataWrapper emptyWrapper() {
		return new EmptyWrapper();
	}

	@Override
	public List<? extends DataWrapper> split(DataWrapper dataToRead2, int splitAtPosition) {
		if(dataToRead2 instanceof ChainedDataWrapper) {
			//A split proxy should never have a reference to a chained one or there is the potential for
			//a memory leak in that as you grow, the right side is not releasing data from ChainedDataWrapper and you end
			//up with  byteWrapper <-chained <- split <-chained <-split <- chained.... and it keeps going as data
			//comes in never releasing the first set of data
			return splitChainedWrapper((ChainedDataWrapper) dataToRead2, splitAtPosition);
		} else if(!(dataToRead2 instanceof SliceableDataWrapper)) {
			throw new IllegalArgumentException("Only SliceableDataWrappers or ChainedDataWrappers are allowed to be split");
		}
		SliceableDataWrapper dataToRead = (SliceableDataWrapper) dataToRead2;
		return splitSliceableWrapper(dataToRead, splitAtPosition);
	}

	List<SliceableDataWrapper> splitSliceableWrapper(SliceableDataWrapper dataToRead, int splitAtPosition) {
		
		List<SliceableDataWrapper> tuple = new ArrayList<>();
		if(splitAtPosition > dataToRead.getReadableSize()) {
			throw new IllegalArgumentException("splitPosition="+splitAtPosition+" is greater than size of data="+dataToRead.getReadableSize());
		} else if(dataToRead.getReadableSize() == 0) {
			tuple.add(new EmptyWrapper());
			tuple.add(new EmptyWrapper());
			return tuple;
		}
		
		SplitProxyWrapper wrapper1 = new SplitProxyWrapper(dataToRead, 0, splitAtPosition);
		
		SliceableDataWrapper wrapper2;
		if(dataToRead.getReadableSize() == splitAtPosition) {
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
