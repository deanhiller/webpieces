package org.webpieces.data.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.webpieces.data.api.BufferPool;

public class ByteBufferDataWrapper extends SliceableDataWrapper {
	
	private ByteBuffer buffer;
	
	public ByteBufferDataWrapper(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	@Override
	public int getReadableSize() {
		return buffer.remaining();
	}

	@Override
	public byte readByteAt(int i) {
		return buffer.get(i);
	}

	@Override
	public String createStringFrom(int offset, int length, Charset charSet) {
		//wanted to go from piece of ByteBuffer to String directly but can't find a good way...
		byte[] data = new byte[length];
		buffer.mark();
		buffer.position(offset);
		//for HeapByteBuffer, this is an array copy and for Mapped, it loops through reading each byte(hopefully from memory).
		buffer.get(data);
		buffer.reset();
		return new String(data, charSet);
	}

	@Override
	public byte[] createByteArray() {
		byte[] data = new byte[buffer.remaining()];
		buffer.mark();
		buffer.get(data);
		buffer.reset();
		return data;
	}

	@Override
	public void addUnderlyingBuffersToList(List<ByteBuffer> buffers) {
		buffers.add(buffer);
	}

	@Override
	public ByteBuffer getSlicedBuffer(int offset, int length) {
		int position = buffer.position();
		int limit = buffer.limit();
		buffer.position(offset);
		buffer.limit(offset+length);
		ByteBuffer theView = buffer.slice();
		buffer.position(position);
		buffer.limit(limit);
		return theView;
	}

	@Override
	protected void releaseImpl(BufferPool pool) {
		buffer.position(buffer.limit());
		pool.releaseBuffer(buffer);
	}

	@Override
	public String toString() {
		return "ByteBufferDataWrapper [remaining=" + buffer.remaining() + ", buf="+buffer+"]";
	}

	@Override
	public String createStringFromUtf8(int offset, int length) {
		return createStringFrom(offset, length, StandardCharsets.UTF_8);
	}

}
