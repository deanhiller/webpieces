package com.webpieces.http2parser.impl;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2StatefulParser;

public class Http2StatefulParserImpl implements Http2StatefulParser {

	private Http2Parser statelessParser;
	private Http2Memento state;

	public Http2StatefulParserImpl(Http2Parser statelessParser, long maxFrameSize) {
		this.statelessParser = statelessParser;
		state = statelessParser.prepareToParse(maxFrameSize);
	}

	@Override
	public DataWrapper marshalToByteBuffer(Http2Frame frame) {
		return statelessParser.marshal(frame);
	}

	@Override
	public List<Http2Frame> parse(DataWrapper moreData) {
		state = statelessParser.parse(state, moreData);
		return state.getParsedFrames();
	}

}
