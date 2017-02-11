package org.webpieces.httpclient.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.HttpChunk;

public interface HttpChunkWriter {

	CompletableFuture<Void> send(HttpChunk chunk);
}
