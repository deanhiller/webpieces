package com.webpieces.http2engine.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.webpieces.javasm.api.Memento;

import com.webpieces.http2engine.api.Http2Payload;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;

public class Level1AggregateHeaders {

	private Map<Integer, Stream> streamIdToStream = new HashMap<>();
	private Level2ClientStateMachine clientSm;
	private Http2Parser2 lowLevelParser;
	private Http2Memento parsingState;

	public Level1AggregateHeaders(Level2ClientStateMachine clientSm, Http2Parser2 lowLevelParser) {
		this.clientSm = clientSm;
		this.lowLevelParser = lowLevelParser;
		parsingState = lowLevelParser.prepareToParse();
	}

	public synchronized CompletableFuture<Void> outgoingFrame(Http2Payload frame) {
		int streamId = frame.getStreamId();

		Stream stream = streamIdToStream.get(streamId);
		if (stream == null) { // idle state
			stream = createStream(streamId);
		}

		Memento currentState = stream.getCurrentState();
		return clientSm.fireSendingFrame(currentState, frame);
	}

	private Stream createStream(int streamId) {
		Memento initialState = clientSm.createStateMachine("stream" + streamId);
		Stream stream = new Stream(initialState);
		streamIdToStream.put(streamId, stream);
		return stream;
	}

	/************************************************************************
	 * Incoming data path only below here
	 *************************************************************************/

	public void incomingData(Http2Payload f) {
		if (f.getStreamId() < 0) {
			throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, f.getStreamId(),
					"frame streamId cannot be less than 0");
		} else if (f.getStreamId() == 0) {
			handleControlPayloads(f);
		} else {
			handleStreamFrame(f);
		}
	}

	private void handleControlPayloads(Http2Payload f) {
		throw new UnsupportedOperationException();
	}

	private void handleStreamFrame(Http2Payload frame) {
		int streamId = frame.getStreamId();
		if (streamId % 2 == 1)
			throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, streamId,
					"Server sent us bad frame id per http/2 spec as in it was an odd id=" + streamId);

		Stream stream = streamIdToStream.get(streamId);
		if (stream == null) {
			createStream(streamId);
		}

		clientSm.fireReceivedFrame(frame);
	}

	public void incomingControlData(Http2Frame lowLevelFrame) {
		int streamId = lowLevelFrame.getStreamId();
		if(streamId != 0)
			throw new IllegalArgumentException("control frame is not stream 0.  streamId="+streamId+" frame type="+lowLevelFrame.getClass());
		
		clientSm.fireControlFrame(lowLevelFrame);
	}

}
