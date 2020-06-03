package com.webpieces.http2.api.dto.lowlevel.lib;

public interface StreamMsg extends Http2Msg {

	boolean isEndOfStream();
	
	int getStreamId();

	void setStreamId(int streamId);
}
