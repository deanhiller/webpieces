package com.webpieces.http2engine.api.client;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.lib.PartialStream;

public interface ClientStreamWriter {

	CompletableFuture<ClientStreamWriter> send(PartialStream data);

}
