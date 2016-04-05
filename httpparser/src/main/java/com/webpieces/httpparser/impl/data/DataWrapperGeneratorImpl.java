package com.webpieces.httpparser.impl.data;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.webpieces.httpparser.api.DataWrapper;
import com.webpieces.httpparser.api.DataWrapperGenerator;

public class DataWrapperGeneratorImpl implements DataWrapperGenerator {

	@Override
	public DataWrapper wrapByteArray(byte[] data) {
		return new ByteArrayDataWrapper(data);
	}

	@Override
	public DataWrapper wrapByteBuffer(ByteBuffer buffer) {
		return new ByteBufferDataWrapper(buffer);
	}

	@Override
	public DataWrapper chainDataWrappers(DataWrapper firstData, DataWrapper secondData) {
		if(firstData instanceof EmptyWrapper) {
			return secondData;
		} else if(firstData instanceof ChainedDataWrapper) {
			ChainedDataWrapper chained = (ChainedDataWrapper) firstData;
			chained.addMoreData(secondData);
			return chained;
		}
		return new ChainedDataWrapper(firstData, secondData);
	}

	@Override
	public DataWrapper emptyWrapper() {
		return new EmptyWrapper();
	}

	@Override
	public List<DataWrapper> split(DataWrapper dataToRead, int splitAtPosition) {
		//let's just split on top of a split for now and not worry about unwinding...
		//We need to check if ChainedDataWrapper and drop certain segments if already read in...
//		if(dataToRead instanceof ChainedDataWrapper) {
//			throw new UnsupportedOperationException("need to add support by unwinding the chain first and making new ones");
//		}
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
}
