package com.webpieces.http2parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.ParserResult;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class Http2MementoImpl implements ParserResult {

	private ParsingState parsingState = ParsingState.NEED_PARSE_FRAME_HEADER;
	private DataWrapper leftOverData;
	private List<Http2Frame> parsedFrames = new ArrayList<>();
	private FrameHeaderData frameHeaderData;

	public Http2MementoImpl(DataWrapper emptyWrapper) {
		this.leftOverData = emptyWrapper;
	}

	public void setParsedFrames(List<Http2Frame> parsedMessages) {
		this.parsedFrames = parsedMessages;
	}

	@Override
	public List<Http2Frame> getParsedFrames() {
		return parsedFrames;
	}

	public DataWrapper getLeftOverData() {
		return leftOverData;
	}

	public void setLeftOverData(DataWrapper allData) {
		this.leftOverData = allData;
	}

	public ParsingState getParsingState() {
		return parsingState;
	}

	public void setParsingState(ParsingState parsingState) {
		this.parsingState = parsingState;
	}

	public void setFrameHeaderData(FrameHeaderData frameHeaderData) {
		this.frameHeaderData = frameHeaderData;
	}

	public FrameHeaderData getFrameHeaderData() {
		return frameHeaderData;
	}

	public void addParsedFrame(AbstractHttp2Frame parsedPayload) {
		parsedFrames.add(parsedPayload);
	}

	@Override
	public DataWrapper getMoreData() {
		return leftOverData;
	}
	
}
