package org.webpieces.nio.impl.cm.basic;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;


public class WriteInfo {

	private ByteBuffer buffer;
	private XFuture<Void> handler;

	public WriteInfo(ByteBuffer b, XFuture<Void> impl) {
		buffer = b;
		handler = impl;
	}

	public XFuture<Void> getPromise() {
		return handler;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

}
