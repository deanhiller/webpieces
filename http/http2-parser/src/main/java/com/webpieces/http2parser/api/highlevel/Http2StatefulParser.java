package com.webpieces.http2parser.api.highlevel;

import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;

public interface Http2StatefulParser {

	CompletableFuture<Void> sendInitialization();

	CompletableFuture<Void> sendFrameOut(Http2Payload frame);

	void cancel(int streamId);

	void parse(DataWrapper newData);


}
