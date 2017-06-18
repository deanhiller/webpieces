package com.webpieces.http2parser.api;

import java.util.List;

import com.webpieces.http2parser.api.dto.lib.Http2Frame;

public interface Http2Memento {

	/**
	 * In the case where you pass in bytes 2 or more messages, we
	 * give you back all the parsed messages so far
	 * @return
	 */
	List<Http2Frame> getParsedFrames();
	
	void setIncomingMaxFrameSize(long maxFrameSize);

	int getLeftOverDataSize();

	int getNumBytesJustParsed();

}
