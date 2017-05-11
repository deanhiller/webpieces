package com.webpieces.http2engine.api.server;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.ConnectionReset;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public interface ServerStreamWriter {

	CompletableFuture<ServerStreamWriter> sendMore(PartialStream data);

}
