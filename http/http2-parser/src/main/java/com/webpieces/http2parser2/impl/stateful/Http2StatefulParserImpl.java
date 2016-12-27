package com.webpieces.http2parser2.impl.stateful;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.ParseException;
import com.webpieces.http2parser.api.dto.Http2ErrorCode;
import com.webpieces.http2parser.api.dto.Http2Frame;
import com.webpieces.http2parser.api.highlevel.Http2Payload;
import com.webpieces.http2parser.api.highlevel.Http2StatefulParser;

public class Http2StatefulParserImpl implements Http2StatefulParser {

	private Http2Parser2 lowLevelParser;
	private Http2Memento state;
	private Map<Integer, Stream> streamIdToStream = new HashMap<>();
	private boolean isServer;

	public Http2StatefulParserImpl(Http2Parser2 lowLevelParser, boolean isServer) {
		this.lowLevelParser = lowLevelParser;
		this.isServer = isServer;
		state = lowLevelParser.prepareToParse();
	}

	@Override
	public DataWrapper marshal(Http2Payload frame) {
		if(frame.getStreamId() < 0)
			throw new IllegalArgumentException("frame streamId cannot be less than 0");
		if(frame.getStreamId() == 0) {
			//TODO: check for bad frames that can't send across 0
		} else if(isServer) {
			if(frame.getStreamId() % 2 == 1)
				throw new IllegalArgumentException("Server cannot send frames with odd stream ids to client per http/2 spec");
		} else if(frame.getStreamId() % 2 == 0)
			throw new IllegalArgumentException("Client cannot send frames with even stream ids to server per http/2 spec");
		
		
		return null;
	}

	/**
	 * NOT thread-safe.  This is meant to be called from a 'virtual' single thread or a single thread.
	 * channelmanager2 uses 'virtual' single threads ensuring order and never running at the same time
	 * but it may not always be the same thread either(to avoid starvation)
	 */
	@Override
	public List<Http2Payload> parse(DataWrapper newData) {
		state = lowLevelParser.parse(state, newData);

		List<Http2Payload> payloadsToReturn = new ArrayList<>();

		List<Http2Frame> frames = state.getParsedMessages();
		for(Http2Frame f : frames) {
			if(f.getStreamId() < 0) {
				throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, f.getStreamId(), "frame streamId cannot be less than 0");
			} else if(f.getStreamId() == 0) {
				handleControlPayloads(f, payloadsToReturn);
			} else {
				handleStreamFrame(f, payloadsToReturn);
			}
		}
		
		return payloadsToReturn;
	}

	private void handleStreamFrame(Http2Frame frame, List<Http2Payload> payloadsToReturn) {
		if(isServer) {
			if(frame.getStreamId() % 2 == 0)
				throw new ParseException(Http2ErrorCode.PROTOCOL_ERROR, frame.getStreamId(), 
						"Client sent us bad frame id per http/2 spec as in it was an even stream id="+frame.getStreamId());
		} else if(frame.getStreamId() % 2 == 1)
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
