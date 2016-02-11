package org.playorm.nio.impl.util;

import java.nio.ByteBuffer;

public class PacketChunk implements DataChunkWithBuffer {

	private ByteBuffer data;
	private DataChunkWithBuffer chunk;

	public PacketChunk(ByteBuffer b, DataChunkWithBuffer chunk) {
		this.data = b;
		this.chunk = chunk;
	}

	@Override
	public ByteBuffer getData() {
		return data;
	}

	@Override
	public void setProcessed(String namedByteConsumerForLogs) {
		chunk.setProcessedImpl();
	}
	
	@Override
	public void setProcessedImpl() {
		chunk.setProcessedImpl();
	}
	
	@Override
	public void releaseBuffer(String logInfo) {
	}
}
