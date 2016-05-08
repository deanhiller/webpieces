package org.webpieces.nio.api.channels;

import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public abstract class ToSocket {

	public abstract void addNext(ToSocket next);
	
	public abstract CompletableFuture<Channel> connect(TCPChannel channel, SocketAddress addr);
	public abstract CompletableFuture<Channel> write(TCPChannel channel, ByteBuffer b);
	public abstract CompletableFuture<Channel> close(TCPChannel channel);
	
}
