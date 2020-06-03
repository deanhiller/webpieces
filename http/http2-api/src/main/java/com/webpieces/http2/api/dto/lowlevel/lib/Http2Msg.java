package com.webpieces.http2.api.dto.lowlevel.lib;

/**
 * This is a full Http2Msg and is NOT one to one with the http2 spec but for 
 * HeadersFrame, PushPromiseFrame, ContinuationFrame has combined them into the full
 * readable piece(ie. all headers present).  For one to one frames, look at Http2Frame
 * and it's subclasses which are all one to one with the spec
 * 
 * @author dhiller
 *
 */
public interface Http2Msg {

	int getStreamId();

	Http2MsgType getMessageType();
	
}
