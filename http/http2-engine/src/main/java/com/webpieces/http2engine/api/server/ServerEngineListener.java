package com.webpieces.http2engine.api.server;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.RequestStreamHandle;
import com.webpieces.http2engine.api.error.ShutdownConnection;

public interface ServerEngineListener {

	/**
	 * A request is either headers only, headers plus data, more data, more data(using StreamWriter), or
	 * headers, data, data, data, headers, OR headers, data, data, stream reset(ie. cancel)
	 */
	RequestStreamHandle openStream();
	
	CompletableFuture<Void> sendToSocket(ByteBuffer newData);

	void closeSocket(ShutdownConnection reason);

}
