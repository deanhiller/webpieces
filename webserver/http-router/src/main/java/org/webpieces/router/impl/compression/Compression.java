package org.webpieces.router.impl.compression;

import java.io.OutputStream;

public interface Compression {

	/**
	 * Returns a stream that compresses data and sends it to the parameter
	 * passed in.
	 * 
	 * @param resultingData
	 * @return
	 */
	OutputStream createCompressionStream(OutputStream resultingData);

	String getCompressionType();
	
}
