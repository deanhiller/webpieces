package com.webpieces.httpparser.impl.data;

import java.nio.ByteBuffer;

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
		return new ChainedDataWrapper(firstData, secondData);
	}

}
