package com.webpieces.httpparser.api;

import java.nio.ByteBuffer;

public interface DataWrapperGenerator {

	public DataWrapper wrapByteArray(byte[] data);
	
	public DataWrapper wrapByteBuffer(ByteBuffer buffer);
	
	/**
	 * Combines two data structures so they can be read as one unit easily from 
	 * client code.  This prevents copying of byte buffers all over the place and just
	 * uses the references already created from the socket whether they are ByteBuffers
	 * or byte[] arrays
	 * 
	 * @param firstData
	 * @param secondData
	 * @return
	 */
	public DataWrapper chainDataWrappers(DataWrapper firstData, DataWrapper secondData);
	
}
