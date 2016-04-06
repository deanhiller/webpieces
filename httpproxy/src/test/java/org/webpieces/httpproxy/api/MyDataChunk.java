package org.webpieces.httpproxy.api;

import java.nio.ByteBuffer;

import org.webpieces.nio.api.handlers.DataChunk;

public class MyDataChunk implements DataChunk {

	private ByteBuffer buffer;
	private boolean processed = false;

	public MyDataChunk(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public ByteBuffer getData() {
		return buffer;
	}

	@Override
	public void setProcessed(String namedByteConsumerForLogs) {
		processed = true;
	}
	
	public boolean getProcessed() {
		return processed;
	}

}
