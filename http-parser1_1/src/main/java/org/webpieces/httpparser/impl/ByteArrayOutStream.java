package org.webpieces.httpparser.impl;

import java.io.IOException;
import java.io.OutputStream;

public class ByteArrayOutStream extends OutputStream {

	private byte[] data;
	private int index = 0;

	public ByteArrayOutStream(byte[] data) {
		this.data = data;
	}
	
	@Override
	public void write(int b) throws IOException {
		data[index] = (byte) b;
	}

	@Override
	public void write(byte[] b) throws IOException {
		super.write(b);
	}

	@Override
	public void write(byte[] b, int off, int len) throws IOException {
		super.write(b, off, len);
	}
	
	

}
