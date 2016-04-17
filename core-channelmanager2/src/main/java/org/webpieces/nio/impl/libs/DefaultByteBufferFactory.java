package org.webpieces.nio.impl.libs;

import java.nio.ByteBuffer;

import org.webpieces.nio.api.libs.BufferFactory;


class DefaultByteBufferFactory implements BufferFactory {

	private boolean isDirect;

	public ByteBuffer createBuffer(Object id, int size) {
		if(isDirect)
			return ByteBuffer.allocateDirect(size);

		return ByteBuffer.allocate(size);
	}

	public void setDirect(boolean b) {
		isDirect = b;
	}

	public boolean isDirect() {
		return isDirect;
	}

}
