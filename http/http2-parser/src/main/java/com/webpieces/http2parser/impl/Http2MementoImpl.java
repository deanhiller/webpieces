package com.webpieces.http2parser.impl;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.lowlevel.lib.AbstractHttp2Frame;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Frame;
import com.webpieces.http2parser.api.Http2Memento;

public class Http2MementoImpl implements Http2Memento {

	private ParsingState parsingState = ParsingState.NEED_PARSE_FRAME_HEADER;
	private DataWrapper leftOverData;
	private List<Http2Frame> parsedFrames = new ArrayList<>();
	private FrameHeaderData frameHeaderData;
	private volatile long maxFrameSize;
	private int numBytesJustParsed = 0;
	private int leftOverDataSize = 0;

	public Http2MementoImpl(DataWrapper emptyWrapper, long maxFrameSize) {
		this.leftOverData = emptyWrapper;
		this.maxFrameSize = maxFrameSize;
	}

	public void setParsedFrames(List<Http2Frame> parsedMessages) {
		this.parsedFrames = parsedMessages;
	}

	@Override
	public List<Http2Frame> getParsedFrames() {
		return parsedFrames;
	}

	public void setLeftOverData(DataWrapper allData, int size) {
		this.leftOverData = allData;
		leftOverDataSize += size;
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

	public DataWrapper getLeftOverData() {
		return leftOverData;
	}

	public long getIncomingMaxFrameSize() {
		return maxFrameSize;
	}

	@Override
	public void setIncomingMaxFrameSize(long maxFrameSize) {
		this.maxFrameSize = maxFrameSize;
	}

	@Override
	public int getNumBytesJustParsed() {
		return numBytesJustParsed;
	}

	public void setNumBytesJustParsed(int numBytesJustParsed) {
		this.numBytesJustParsed = numBytesJustParsed;
	}

	public int getLeftOverDataSize() {
		return leftOverDataSize;
	}

	public void setLeftOverDataSize(int leftOverDataSize) {
		this.leftOverDataSize = leftOverDataSize;
	}
	
}
