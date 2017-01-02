package com.webpieces.http2engine.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.dto.PartialStream;

public interface RequestWriter {

	CompletableFuture<RequestWriter> sendMore(PartialStream data);

}
