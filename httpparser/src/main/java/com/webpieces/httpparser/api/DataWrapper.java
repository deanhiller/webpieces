package com.webpieces.httpparser.api;

import java.nio.charset.Charset;

/**
 * NOTE: I feel like this should be moved down into ChannelManager? such that no clients ever
 * need to 'copy' data but could read two packets that came in as they were one
 * 
 * For now, let's just get something in place
 * 
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
	 * debugging scenarios.  This can be a costly operation.
	 * @return
	 */
	public byte[] createByteArray();
	
	//Later, we may want this as well...
	//public InputStream createInputStreamFrom();
	
}
