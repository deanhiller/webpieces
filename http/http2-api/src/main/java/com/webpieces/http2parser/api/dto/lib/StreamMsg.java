package com.webpieces.http2parser.api.dto.lib;

public interface StreamMsg extends Http2Msg {

	boolean isEndOfStream();
	
	int getStreamId();

	void setStreamId(int streamId);
}
