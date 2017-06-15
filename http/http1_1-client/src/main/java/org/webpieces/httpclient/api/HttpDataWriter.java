package org.webpieces.httpclient.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.HttpData;

public interface HttpDataWriter {

	CompletableFuture<Void> send(HttpData chunk);
}
