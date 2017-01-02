package org.webpieces.http2client.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.dto.PartialStream;

public interface Http2SocketDataWriter {

	CompletableFuture<Http2SocketDataWriter> sendData(PartialStream data);
	
}
