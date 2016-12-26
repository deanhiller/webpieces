package com.webpieces.http2parser2.impl;

import java.util.ArrayList;
import java.util.List;

import org.webpieces.data.api.DataWrapper;

import com.twitter.hpack.Decoder;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.Http2ParsedStatus;
import com.webpieces.http2parser.api.Http2SettingsMap;
import com.webpieces.http2parser.api.dto.Http2Payload;

public class Http2MementoImpl implements Http2Memento {

	private ParsingState parsingState = ParsingState.NEED_PARSE_FRAME_HEADER;
	private Decoder decoder;
	private Http2SettingsMap localSettings;
	private Http2SettingsMap remoteSettings;
	private DataWrapper leftOverData;
	private List<Http2Payload> parsedMessages = new ArrayList<>();
	private Http2ParsedStatus parsedStatus;
	private FrameHeaderData frameHeaderData;

	public Http2MementoImpl(Decoder decoder, Http2SettingsMap http2SettingsMap, DataWrapper emptyWrapper) {
		this.decoder = decoder;
		this.localSettings = http2SettingsMap;
		this.leftOverData = emptyWrapper;
	}

	@Override
	public Http2ParsedStatus getParsedStatus() {
		return parsedStatus;
	}

	public void setParsedMessages(List<Http2Payload> parsedMessages) {
		this.parsedMessages = parsedMessages;
	}

	public void setParsedStatus(Http2ParsedStatus parsedStatus) {
		this.parsedStatus = parsedStatus;
	}

	@Override
	public List<Http2Payload> getParsedMessages() {
		return parsedMessages;
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

	public void addParsedPayload(Http2Payload parsedPayload) {
		parsedMessages.add(parsedPayload);
	}
	
}
