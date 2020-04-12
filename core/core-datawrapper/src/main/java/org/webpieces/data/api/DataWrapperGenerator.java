package org.webpieces.data.api;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * A library that HIDES ByteBuffer[] so that you don't have to copy data on processing to save on
 * performance of GC and other stuff.  Unfortunately, there is no interface on ByteBuffer that we
 * could implement due to some jdk reasons for doing direct memory.  Of course, they could have
 * written a higher level library hiding the details as copying data is still not cheap.
 * 
 * @author dean
 *
 */
public interface DataWrapperGenerator {

	DataWrapper emptyWrapper();
	
	DataWrapper wrapByteArray(byte[] data);
	
	DataWrapper wrapByteArray(byte[] data, int i, int read);

	DataWrapper wrapByteBuffer(ByteBuffer buffer);

	DataWrapper wrapString(String string);
	
	/**
	 * Combines two data structures so they can be read as one unit easily from 
	 * client code.  This prevents copying of byte buffers all over the place and just
	 * uses the references already created from the socket whether they are ByteBuffers
	 * or byte[] arrays
	 * 
	 * @param begin to be put at the front
	 * @param end to be put at the end
	 * @return
	 */
	DataWrapper chainDataWrappers(DataWrapper begin, DataWrapper end);
	
	DataWrapper chainDataWrappers(DataWrapper firstData, DataWrapper ... secondData);

	DataWrapper chainDataWrappers(DataWrapper begin, ByteBuffer... end);
	
	/**
	 * In some cases, we may have a DataWrapper containing the end of the message 
	 * so we need a DataWrapper with the 1st complete message and a 2nd wrapper with
	 * the leftover data.  When we split, no copying of bytes should be necessary and
	 * we should peak inside ChainedDataWrappers to unwind those so we don't keep
	 * stacking elements up.
	 * 
	 * @param dataToRead
	 * @param splitAtPosition
	 * @return
	 */
	List<? extends DataWrapper> split(DataWrapper dataToRead, int splitAtPosition);

	
}
