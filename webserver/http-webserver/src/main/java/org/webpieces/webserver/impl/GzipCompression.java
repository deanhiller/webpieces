package org.webpieces.webserver.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

public class GzipCompression implements Compression {

	@Override
	public byte[] compress(byte[] bytes) {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(bytes.length);
		try (GZIPOutputStream zipStream = new GZIPOutputStream(byteStream)) {
			zipStream.write(bytes);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return byteStream.toByteArray();
	}
}
