package com.webpieces.hpack.api;

import java.util.List;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Msg;

public interface UnmarshalState {

	/**
	 * In the case where you pass in bytes of 2 or more messages, we
	 * give you back all the parsed messages so far
	 * @return
	 */
	List<Http2Msg> getParsedFrames();
	
	int getLeftOverDataSize();
	
	int getNumBytesJustParsed();
	
    void setDecoderMaxTableSize(int newSize);
    void setIncomingMaxFrameSize(long maxFrameSize);
}
