package org.webpieces.httpclient11.impl;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.handlers.DataListener;

public interface ChannelProxy {

	CompletableFuture<Void> connect(InetSocketAddress addr, DataListener dataListener);

	CompletableFuture<Void> write(ByteBuffer wrap);

	CompletableFuture<Void> close();

	String getId();

	boolean isSecure();

}
