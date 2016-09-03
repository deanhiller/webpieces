package org.webpieces.router.impl.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class GzipCompression implements Compression {

	@Override
	public OutputStream createCompressionStream(OutputStream resultingData) {
		try {
			return new GZIPOutputStream(resultingData);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public String getCompressionType() {
		return "gzip";
	}
}
