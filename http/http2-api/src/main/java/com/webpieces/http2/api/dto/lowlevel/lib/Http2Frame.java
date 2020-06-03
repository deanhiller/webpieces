package com.webpieces.http2.api.dto.lowlevel.lib;

/**
 * This class and it's subclasses are one to one with the http2 spec in that each Frame is a one to one with
 * the binary protocol message including the HeadersFrame, ContinuationFrame, PushPromiseFrame which are only
 * pieces!!!  If you want the full pieced to together http2 messages, then use Http2Msg
 * 
 * @author dhiller
 *
 */
public interface Http2Frame {

	int getStreamId();

	void setStreamId(int id);

	Http2FrameType getFrameType();
}
