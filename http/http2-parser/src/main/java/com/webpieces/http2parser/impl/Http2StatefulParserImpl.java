package com.webpieces.http2parser.impl;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2StatefulParser;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class Http2StatefulParserImpl implements Http2StatefulParser {

	private Http2Parser statelessParser;
	private Http2Memento state;

	public Http2StatefulParserImpl(Http2Parser statelessParser) {
		this.statelessParser = statelessParser;
		state = statelessParser.prepareToParse();
	}

	@Override
	public DataWrapper marshalToByteBuffer(Http2Frame frame) {
		return statelessParser.marshal(frame);
	}

	@Override
	public List<Http2Frame> parse(DataWrapper moreData, long maxFrameSize) {
		state = statelessParser.parse(state, moreData, maxFrameSize);
		return state.getParsedFrames();
	}

}
