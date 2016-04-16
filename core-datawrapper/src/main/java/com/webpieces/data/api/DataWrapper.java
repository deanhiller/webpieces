package com.webpieces.data.api;

import java.nio.charset.Charset;

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

	public String createStringFrom(int offset, int length, Charset charSet);

	/**
	 * This may or may not copy all the underlying data and is only meant to be used in
	 * debugging scenarios.  This can be a costly operation.  It is deprecated from day 1
     *
	 * @deprecated
	 */
	@Deprecated
	public byte[] createByteArray();
	
	@Deprecated
	public int getNumLayers();

}
