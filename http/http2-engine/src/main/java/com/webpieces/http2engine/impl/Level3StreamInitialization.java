package com.webpieces.http2engine.impl;

import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.Http2ResponseListener;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.RequestWriter;
import com.webpieces.http2engine.api.dto.Http2Headers;
import com.webpieces.http2engine.api.dto.Http2Push;
import com.webpieces.http2engine.api.dto.PartialStream;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.lib.Http2ErrorCode;

public class Level3StreamInitialization {

	private Level4ClientStateMachine clientSm;
	//all state(memory) we need to clean up is in here or is the engine itself.  To release RAM,
	//we have to release items in the map inside this or release the engine
	private StreamState streamState = new StreamState();

	public Level3StreamInitialization(Level4ClientStateMachine clientSm) {
		this.clientSm = clientSm;
	}

	public synchronized CompletableFuture<RequestWriter> createStreamAndSend(Http2Headers frame, Http2ResponseListener responseListener) {
		int streamId = frame.getStreamId();
		if (streamState.get(streamId) != null) { // idle state
			throw new IllegalStateException("Stream with id="+streamId+" already exists and can't be created again");
		}

		Stream stream = createStream(streamId, responseListener, null);
		streamState.put(streamId, stream);

		Memento currentState = stream.getCurrentState();
		return clientSm.fireToSocket(currentState, frame)
				.thenApply(c -> new RequestWriterImpl(stream, clientSm));
	}
	
	private Stream createStream(int streamId, Http2ResponseListener responseListener, PushPromiseListener pushListener) {
		Memento initialState = clientSm.createStateMachine("stream" + streamId);
		Stream stream = new Stream(streamId, initialState, responseListener, pushListener);
		streamState.put(streamId, stream);
		return stream;
	}
	
	public void sendPayloadToClient(PartialStream frame) {
		int streamId = frame.getStreamId();

		Stream stream = streamState.get(streamId);
		if (stream == null)
			throw new IllegalArgumentException("bug, Stream not found for frame="+frame);

		Memento currentState = stream.getCurrentState();
		clientSm.testStateMachineTransition(currentState, frame);
		
		if(frame.getStreamId() % 2 == 1) {
			Http2ResponseListener listener = stream.getResponseListener();
			listener.incomingPartialResponse(frame);
		} else {
			PushPromiseListener listener = stream.getPushListener();
			listener.incomingPushPromise(frame);
		}
	}

	public void sendPushPromiseToClient(Http2Push fullPromise) {		
		int newStreamId = fullPromise.getStreamId();
		if(newStreamId % 2 == 1)
			throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, newStreamId, 
					"Server sent bad push promise="+fullPromise+" as new stream id is incorrect and is an odd number", true);

		Stream stream = streamState.get(newStreamId);
		if (stream != null) {
			throw new IllegalArgumentException("bug, how does the stream exist already. promise="+fullPromise);
		}
		Stream causalStream = streamState.get(fullPromise.getCausalStreamId());
		if(causalStream == null)
			throw new IllegalArgumentException("bug, the stream causing the push_promise does not exist. promise="+fullPromise);
		
		Http2ResponseListener listener = causalStream.getResponseListener();
		PushPromiseListener pushListener = listener.newIncomingPush(newStreamId);

		stream = createStream(newStreamId, null, pushListener);
		Memento currentState = stream.getCurrentState();
		clientSm.testStateMachineTransition(currentState, fullPromise);
		pushListener.incomingPushPromise(fullPromise);
	}
}
