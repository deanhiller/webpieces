package org.webpieces.http2client.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.handlers.DataListener;

public interface Http2ChannelProxy {

	CompletableFuture<Void> write(ByteBuffer data);

	CompletableFuture<Void> connect(InetSocketAddress addr, DataListener listener);

	CompletableFuture<Void> close();

}
