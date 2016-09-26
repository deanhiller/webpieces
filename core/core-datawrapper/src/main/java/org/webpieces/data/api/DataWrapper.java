package org.webpieces.data.api;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;

/**
 * This data structure is designed to not need to copy tons of payloads from bytes
 * to bytes such that two byte[] or two ByteBuffer can be read from like they are
 * just one entity. 
 * 
 * @author dhiller
 *
 */
public interface DataWrapper {

	public int getReadableSize();
	
	public byte readByteAt(int i);

	public byte[] readBytesAt(int i, int len);

	public String createStringFrom(int offset, int length, Charset charSet);

	/**
	 * This may or may not copy all the underlying data and is mostly for
	 * debugging scenarios, or in some cases where you are using an api that 
	 * takes a byte[]  (NOTE: you probably have worse places in your code on performance so 
	 * it's not really that big of a deal)
	 */
	public byte[] createByteArray();
	
	@Deprecated
	public int getNumLayers();

	/**
	 * This is so you can feed into 3rd party libraries that then should tell you how many
	 * bytes were read(much like SSLEngine) and after they tell you how much was read, you 
	 * should then proceed to call DataWrapperGenerator.split(dataWrapper, sizeRead) and most
	 * likely can discard the one of the DataWrappers resulting in that one and the ByteBuffers
	 * it holds to be garbage collected
	 * 
	 */
	public void addUnderlyingBuffersToList(List<ByteBuffer> buffers);
	
	public void releaseUnderlyingBuffers(BufferPool pool);
}
