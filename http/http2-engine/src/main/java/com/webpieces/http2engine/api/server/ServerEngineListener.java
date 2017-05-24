package com.webpieces.http2engine.api.server;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamHandle;
import com.webpieces.http2parser.api.dto.error.Http2Exception;

public interface ServerEngineListener {

	/**
	 * A request is either headers only, headers plus data, more data, more data(using StreamWriter), or
	 * headers, data, data, data, headers, OR headers, data, data, stream reset(ie. cancel)
	 */
	StreamHandle openStream(int streamId, ResponseHandler2 responseHandler);
	
	CompletableFuture<Void> sendToSocket(ByteBuffer newData);

	void closeSocket(Http2Exception reason);

}
