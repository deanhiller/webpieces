package org.webpieces.data.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;

public class DataWrapperGeneratorImpl implements DataWrapperGenerator {

	private static final ByteBufferDataWrapper EMPTY_WRAPPER = new ByteBufferDataWrapper(ByteBuffer.allocate(0));

	@Override
	public DataWrapper wrapByteArray(byte[] data) {
		return new ByteBufferDataWrapper(ByteBuffer.wrap(data));
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
	public DataWrapper chainDataWrappers(DataWrapper firstData, ByteBuffer...secondData) {
		DataWrapper wrapper = firstData;
		for(ByteBuffer buffer : secondData) {
			DataWrapper second = wrapByteBuffer(buffer);
			wrapper = chainDataWrappers(wrapper, second);
		}
		return wrapper;
	}
	
	@Override
	public DataWrapper chainDataWrappers(DataWrapper firstData, DataWrapper secondData) {
		if(firstData.getReadableSize() == 0) {
			return secondData;
		} else if(secondData.getReadableSize() == 0) {
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

		return new ChainedDataWrapper(first, second);
	}

	@Override
	public DataWrapper emptyWrapper() {
		return EMPTY_WRAPPER;
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
			tuple.add(EMPTY_WRAPPER);
			tuple.add(EMPTY_WRAPPER);
			return tuple;
		}
		
		SplitProxyWrapper wrapper1 = new SplitProxyWrapper(dataToRead, 0, splitAtPosition);
		
		SliceableDataWrapper wrapper2;
		if(dataToRead.getReadableSize() == splitAtPosition) {
			wrapper2 = EMPTY_WRAPPER;
		} else {
			dataToRead.increaseRefCount();
			wrapper2 = 
				new SplitProxyWrapper(dataToRead, splitAtPosition, dataToRead.getReadableSize() - splitAtPosition);
		}
		
		tuple.add(wrapper1);
		tuple.add(wrapper2);
		
		return tuple;
	}

	private List<DataWrapper> splitChainedWrapper(ChainedDataWrapper dataToRead, int splitAtPosition) {
		List<SliceableDataWrapper> wrappersInBegin = new ArrayList<>();
		List<SliceableDataWrapper> wrappersInEnd = new ArrayList<>();
		
		boolean foundSplit = false;
		List<SliceableDataWrapper> splitBuffers = null;
		for(SliceableDataWrapper wrapper : dataToRead.getWrappers()) {
			if (!foundSplit) {
				if(splitAtPosition == wrapper.getReadableSize()) {
					wrappersInBegin.add(wrapper);
					foundSplit = true;
				} else if(splitAtPosition < wrapper.getReadableSize()) {
					splitBuffers = splitSliceableWrapper(wrapper, splitAtPosition);
					wrappersInBegin.add(splitBuffers.get(0));
					wrappersInEnd.add(splitBuffers.get(1));
					foundSplit = true;
				} else {
					wrappersInBegin.add(wrapper);
					splitAtPosition = splitAtPosition - wrapper.getReadableSize();	
				}
			} else {
				wrappersInEnd.add(wrapper);
			}
		}

		DataWrapper wrapper1;
		if(wrappersInBegin.size() > 0) 
			wrapper1 = new ChainedDataWrapper(wrappersInBegin);
		else 
			wrapper1 = EMPTY_WRAPPER;
		
		DataWrapper wrapper2;
		if(wrappersInEnd.size() > 0) 
			wrapper2 = new ChainedDataWrapper(wrappersInEnd);
		else
			wrapper2 = EMPTY_WRAPPER;
		
		List<DataWrapper> finalTwo = new ArrayList<>();
		finalTwo.add(wrapper1);
		finalTwo.add(wrapper2);
		return finalTwo;
	}
}
