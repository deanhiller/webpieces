package org.webpieces.router.impl.compression;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import org.webpieces.util.SneakyThrow;

public class GzipCompression implements Compression {

	@Override
	public OutputStream createCompressionStream(OutputStream resultingData) {
		try {
			return new GZIPOutputStream(resultingData);
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}

	@Override
	public String getCompressionType() {
		return "gzip";
	}
}
