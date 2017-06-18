package com.webpieces.hpack.impl;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.twitter.hpack.Decoder;
import com.webpieces.hpack.api.UnmarshalState;
import com.webpieces.http2parser.api.Http2Memento;
import com.webpieces.http2parser.api.dto.lib.HasHeaderFragment;
import com.webpieces.http2parser.api.dto.lib.Http2Msg;

public class UnmarshalStateImpl implements UnmarshalState {

	private HeaderDecoding decoding;
	private Decoder decoder;
	private Http2Memento lowLevelState;
	
    private List<HasHeaderFragment> headersToCombine = new LinkedList<>();
	private List<Http2Msg> parsedFrames = new ArrayList<>();
	private String logId;
	private int numBytesJustParsed = 0;
	private int dataToParseSize = 0;
	private int halfParsedSize;
	
	public UnmarshalStateImpl(String logId, Http2Memento lowLevelState, HeaderDecoding decoding, Decoder decoder) {
		this.logId = logId;
		this.lowLevelState = lowLevelState;
		this.decoding = decoding;
		this.decoder = decoder;
	}

	@Override
	public List<Http2Msg> getParsedFrames() {
		return parsedFrames;
	}

	public Http2Memento getLowLevelState() {
		return lowLevelState;
	}

	public List<HasHeaderFragment> getHeadersToCombine() {
		return headersToCombine;
	}

	public Decoder getDecoder() {
		return decoder;
	}

	@Override
	public void setDecoderMaxTableSize(int newSize) {
		decoding.setMaxHeaderTableSize(decoder, newSize);
	}

	@Override
	public void setIncomingMaxFrameSize(long maxFrameSize) {
		lowLevelState.setIncomingMaxFrameSize(maxFrameSize);
	}

	public void clearParsedFrames() {
		parsedFrames = new ArrayList<>();
	}

	public String getLogId() {
		return logId;
	}

	@Override
	public int getLeftOverDataSize() {
		return dataToParseSize;
	}
	
	@Override
	public int getNumBytesJustParsed() {
		return numBytesJustParsed;
	}

	public int getDataToParseSize() {
		return dataToParseSize;
	}

	public void addToDataToParseSize(int readableSize) {
		dataToParseSize += readableSize;
	}

	public void addHalfParsedSize(int numBytesJustParsed2) {
		halfParsedSize += numBytesJustParsed2;
	}

	public void addParsedMessage(Http2Msg frame) {
		numBytesJustParsed += halfParsedSize;
		dataToParseSize -= halfParsedSize;
		parsedFrames.add(frame);
		halfParsedSize = 0;
	}

	public void resetNumBytesJustParsed() {
		numBytesJustParsed = 0;
	}

}
