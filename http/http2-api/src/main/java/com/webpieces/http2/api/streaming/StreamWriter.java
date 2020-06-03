package com.webpieces.http2.api.streaming;

import java.util.concurrent.CompletableFuture;

import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;

public interface StreamWriter {

	CompletableFuture<Void> processPiece(StreamMsg data);

}