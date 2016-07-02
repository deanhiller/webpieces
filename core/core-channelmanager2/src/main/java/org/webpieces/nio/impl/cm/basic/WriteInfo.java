package org.webpieces.nio.impl.cm.basic;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;


public class WriteInfo {

	private ByteBuffer buffer;
	private CompletableFuture<Channel> handler;

	public WriteInfo(ByteBuffer b, CompletableFuture<Channel> impl) {
		buffer = b;
		handler = impl;
	}

	public CompletableFuture<Channel> getPromise() {
		return handler;
	}

	public ByteBuffer getBuffer() {
		return buffer;
	}

}
