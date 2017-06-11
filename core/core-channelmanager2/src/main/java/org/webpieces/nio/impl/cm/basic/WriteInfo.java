package org.webpieces.nio.impl.cm.basic;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;


public class WriteInfo {

	private ByteBuffer buffer;
	private CompletableFuture<Void> handler;

	public WriteInfo(ByteBuffer b, CompletableFuture<Void> impl) {
		buffer = b;
		handler = impl;
	}

	public CompletableFuture<Void> getPromise() {
		return handler;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

}
