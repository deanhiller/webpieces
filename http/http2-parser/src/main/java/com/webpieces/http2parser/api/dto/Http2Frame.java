package com.webpieces.http2parser.api.dto;

public interface Http2Frame {

	int getStreamId();

	void setStreamId(int id);

	Http2FrameType getFrameType();
}
