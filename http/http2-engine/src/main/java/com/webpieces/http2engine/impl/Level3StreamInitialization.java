package com.webpieces.http2engine.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.Http2Push;
import com.webpieces.http2engine.api.PartialStream;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

public class Level3StreamInitialization {

	private Level4ClientStateMachine clientSm;

	private Map<Integer, Stream> streamIdToStream = new HashMap<>();

	public Level3StreamInitialization(Level4ClientStateMachine clientSm) {
		this.clientSm = clientSm;
	}

	public synchronized CompletableFuture<Void> outgoingFrame(PartialStream frame) {
		int streamId = frame.getStreamId();

		Stream stream = streamIdToStream.get(streamId);
		if (stream == null) { // idle state
			stream = createStream(streamId);
		}

		Memento currentState = stream.getCurrentState();
		return clientSm.fireToSocket(currentState, frame);
	}

	private Stream createStream(int streamId) {
		Memento initialState = clientSm.createStateMachine("stream" + streamId);
		Stream stream = new Stream(initialState);
		streamIdToStream.put(streamId, stream);
		return stream;
	}
	
	public void sendPayloadToClient(PartialStream frame) {
		int streamId = frame.getStreamId();

		Stream stream = streamIdToStream.get(streamId);
		if (stream == null)
			throw new IllegalArgumentException("bug, Stream not found for frame="+frame);

		Memento currentState = stream.getCurrentState();
		clientSm.fireToClient(currentState, frame);
	}

	public void sendPushPromiseToClient(Http2Push fullPromise) {		
		int newStreamId = fullPromise.getStreamId();
		if(newStreamId % 2 == 1)
			throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, newStreamId, 
					"Server sent bad push promise="+fullPromise+" as new stream id is incorrect");

		Stream stream = streamIdToStream.get(newStreamId);
		if (stream != null) {
			throw new IllegalArgumentException("bug, how does the stream exist already");
		}

		stream = createStream(newStreamId);
		Memento currentState = stream.getCurrentState();
		clientSm.fireToClient(currentState, fullPromise);
	}
}
