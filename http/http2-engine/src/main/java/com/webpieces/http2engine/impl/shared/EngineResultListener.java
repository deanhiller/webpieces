package com.webpieces.http2engine.impl.shared;

import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2parser.api.Http2Exception;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public interface EngineResultListener {


	CompletableFuture<Void> sendControlFrameToClient(Http2Msg msg);

	void farEndClosed();
	
	void closeSocket(Http2Exception reason);

	CompletableFuture<Void> sendToSocket(ByteBuffer buffer);

	CompletableFuture<Void> sendPieceToClient(Stream stream, PartialStream payload);

//	/**
//	 * Data comes in as a single Http2Headers, then many Http2Data if there is a payload, then a
//	 * trailing SINGLE Http2Headers.  At any time, you can call response.isLastPartOfResponse
//	 * to see if it is the final end of the response
//	 * 
//	 * @param resp
//	 * @return Future that will complete when we should free up more space to read more incoming data for this HttpEngine
//	 */
//	CompletableFuture<Void> incomingPartialResponse(Stream s, PartialStream response);
//
//	/**
//	 * For http/2 only in that servers can pre-emptively send a response to requests
//	 * that are about to happen based on the first request.  The Http/2 Engine will call this
//	 * method and then invoke the methods in your instance multiple times.  It is best you return
//	 * a new implementation each time as each push is separate and can be intermingled as well.
//	 * 
//	 * @param req
//	 * @param resp
//	 * @param isComplete
//	 */
//	PushPromiseListener newIncomingPush(int streamId);
}
