package com.webpieces.http2parser2.impl.stateful;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.javasm.api.Event;
import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.State;
import org.webpieces.javasm.api.StateMachine;
import org.webpieces.javasm.api.StateMachineFactory;

import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.highlevel.Http2FullHeaders;
import com.webpieces.http2parser.api.highlevel.Http2Payload;
import com.webpieces.http2parser.api.highlevel.Http2StatefulParser;
import com.webpieces.http2parser.api.highlevel.ToClient;
import com.webpieces.http2parser.api.highlevel.ToSocket;

public class Http2ClientParserImpl implements Http2StatefulParser {
	
	private Http2Parser2 lowLevelParser;
	private Map<Integer, Stream> streamIdToStream = new HashMap<>();
	private int latestStream = 1;
	private ClientStateMachine clientSm;
	private Http2Memento parsingState;

	public Http2ClientParserImpl(String id, Http2Parser2 lowLevelParser, ToClient clientListener, ToSocket socketListener) {
		this.lowLevelParser = lowLevelParser;
		parsingState = lowLevelParser.prepareToParse();
		
		clientSm = new ClientStateMachine(id);
	}

	@Override
	public void marshal(Http2Payload frame) {
		int streamId = frame.getStreamId();
		if(streamId < 0)
			throw new IllegalArgumentException("frame streamId cannot be less than 0");
		if(streamId == 0) {
			marshalControlFrame(frame);
			return;
		} else if(streamId % 2 == 0)
			throw new IllegalArgumentException("Client cannot send frames with even stream ids to server per http/2 spec");

		validate(frame);
		
		sendFrame(frame);
		
	}

	private synchronized void validate(Http2Payload frame) {
		int streamId = frame.getStreamId();

		Stream stream = streamIdToStream.get(streamId);
		if(stream == null) { //idle state
			if(!(frame instanceof Http2FullHeaders))
				throw new IllegalArgumentException("To start a stream with streamid="+streamId+", the first frame must be Http2FullHeaders");
			createStream(streamId);
			return;
		}
		
		Memento currentState = stream.getCurrentState();
		
		
		clientSm.fireReceivedEvent(frame.getClass());
	}

	private void createStream(int streamId) {
		Memento initialState = clientSm.createStateMachine("stream"+streamId);
		streamIdToStream.put(streamId, new Stream(initialState));
	}

	private void sendFrame(Http2Payload frame) {
	}
	
	private void marshalControlFrame(Http2Payload frame) {
		//TODO: check for bad frames that can't send across 0
		
	}

	/**
	 * NOT thread-safe.  This is meant to be called from a 'virtual' single thread or a single thread.
	 * channelmanager2 uses 'virtual' single threads ensuring order and never running at the same time
	 * but it may not always be the same thread either(to avoid starvation)
	 */
	@Override
	public void parse(DataWrapper newData) {
		parsingState = lowLevelParser.parse(parsingState, newData);

		List<Http2Payload> payloadsToReturn = new ArrayList<>();

		List<Http2Frame> frames = parsingState.getParsedMessages();
		for(Http2Frame f : frames) {
			if(f.getStreamId() < 0) {
				throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, f.getStreamId(), "frame streamId cannot be less than 0");
			} else if(f.getStreamId() == 0) {
				handleControlPayloads(f, payloadsToReturn);
			} else {
				handleStreamFrame(f, payloadsToReturn);
			}
		}
		
	}

	private void handleStreamFrame(Http2Frame frame, List<Http2Payload> payloadsToReturn) {
		if(frame.getStreamId() % 2 == 1)
			throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, frame.getStreamId(), 
					"Server sent us bad frame id per http/2 spec as in it was an odd id="+frame.getStreamId());

		Stream stream = streamIdToStream.get(frame.getStreamId());
		if(stream == null) {
			stream = new Stream();
			streamIdToStream.put(frame.getStreamId(), stream);
		}
		
		stream.addFrame(frame);
		
		payloadsToReturn.add(stream.getPayloads());
		
		if(stream.isClosed()) {
			streamIdToStream.remove(frame.getStreamId());
		}
	}

	private void handleControlPayloads(Http2Frame f, List<Http2Payload> payloadsToReturn) {

	}

}
