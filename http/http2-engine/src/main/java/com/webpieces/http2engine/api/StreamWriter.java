package com.webpieces.http2engine.api;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.lib.StreamMsg;

public interface StreamWriter {

	CompletableFuture<StreamWriter> processPiece(StreamMsg data);

}