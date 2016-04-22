package org.webpieces.netty.api;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;

public class BufferPool {
	private Map<ByteBuffer, ReferenceInfo> refInfo = new HashMap<>();

	public synchronized void releaseBuffer(ByteBuffer buffer) {
		ReferenceInfo ref = refInfo.remove(buffer);
		if(ref == null)
			throw new IllegalArgumentException("This buffer was not given to you to be released");
		
		ref.releaseOneCount();
	}
	
	public void recordBufferToBufMappingForRelease(ByteBuffer[] buffers, ByteBuf buf) {
		ReferenceInfo referenceInfo = new ReferenceInfo(buffers.length, buf);
		for(ByteBuffer buffer : buffers) {
			refInfo.put(buffer, referenceInfo);
		}
	}
	
	private static class ReferenceInfo {
		private ByteBuf ref;
		private int count;
		
		public ReferenceInfo(int length, ByteBuf buf) {
			this.count = length;
			this.ref = buf;
		}

		public void releaseOneCount() {
			count--;
			if(count > 0)
				return;
			
			ref.release();
		}
	}
}
