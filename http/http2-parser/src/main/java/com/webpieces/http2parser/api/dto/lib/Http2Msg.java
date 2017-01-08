package com.webpieces.http2parser.api.dto.lib;

public interface Http2Msg {

	int getStreamId();

	Http2MsgType getMessageType();
	
}
