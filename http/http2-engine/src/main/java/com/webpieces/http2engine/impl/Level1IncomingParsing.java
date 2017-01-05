package com.webpieces.http2engine.impl;

import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class Level1IncomingParsing {

	private Level2AggregateDecodeHeaders headers;
	private Http2Parser lowLevelParser;
	private Http2Memento parsingState;
	private HeaderSettings remoteSettings;

	public Level1IncomingParsing(Level2AggregateDecodeHeaders headers, Http2Parser lowLevelParser, HeaderSettings remoteSettings) {
		this.headers = headers;
		this.lowLevelParser = lowLevelParser;
		this.remoteSettings = remoteSettings;
		parsingState = lowLevelParser.prepareToParse();
	}

	public void parse(DataWrapper newData) {
		parsingState = lowLevelParser.parse(parsingState, newData, remoteSettings.getMaxFrameSize());
		List<Http2Frame> parsedMessages = parsingState.getParsedFrames();
		
		for(Http2Frame lowLevelFrame : parsedMessages) {
			headers.sendFrameUpToClient(lowLevelFrame);
		}
	}
}
