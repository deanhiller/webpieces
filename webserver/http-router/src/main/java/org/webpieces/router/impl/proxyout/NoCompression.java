package org.webpieces.router.impl.proxyout;

import java.io.OutputStream;

import org.webpieces.router.impl.compression.Compression;

public class NoCompression implements Compression {

	@Override
	public OutputStream createCompressionStream(OutputStream resultingData) {
		return resultingData;
	}

	@Override
	public String getCompressionType() {
		return null;
	}

}
