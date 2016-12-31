package org.webpieces.http2client.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.dto.Http2Headers;

public interface Http2SocketDataWriter {

	/**
	 * Send HttpLastChunk to end the streaming of the chunks
	 * @param resp
	 */
	CompletableFuture<Http2SocketDataWriter> sendData(DataWrapper data, boolean isComplete);

	CompletableFuture<Http2SocketDataWriter> sendTrailingHeaders(Http2Headers endHeaders);

	/**
	 * In http/2, sends a stream reset to cancel the request.  In http1.1, throws an exception
	 * since requests can't be cancelled
	 */
	void cancel();
	
}
