package org.webpieces.httpclient.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.HttpData;

public interface HttpChunkWriter {

	CompletableFuture<HttpChunkWriter> send(HttpData chunk);
}
