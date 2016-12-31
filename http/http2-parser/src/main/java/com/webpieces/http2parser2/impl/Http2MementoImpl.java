package com.webpieces.http2parser2.impl;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2ParsedStatus;
import com.webpieces.http2parser.api.dto.lib.AbstractHttp2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public class Http2MementoImpl implements Http2Memento {

	private ParsingState parsingState = ParsingState.NEED_PARSE_FRAME_HEADER;
	private DataWrapper leftOverData;
	private List<Http2Frame> parsedFrames = new ArrayList<>();
	private Http2ParsedStatus parsedStatus;
	private FrameHeaderData frameHeaderData;

	public Http2MementoImpl(DataWrapper emptyWrapper) {
		this.leftOverData = emptyWrapper;
	}

	@Override
	public Http2ParsedStatus getParsedStatus() {
		return parsedStatus;
	}

	public void setParsedFrames(List<Http2Frame> parsedMessages) {
		this.parsedFrames = parsedMessages;
	}

	public void setParsedStatus(Http2ParsedStatus parsedStatus) {
		this.parsedStatus = parsedStatus;
	}

	@Override
	public List<Http2Frame> getParsedMessages() {
		return parsedFrames;
	}

	@Override
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
	
}
