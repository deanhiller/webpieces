package com.webpieces.http2.api.dto.highlevel;

import java.util.List;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2MsgType;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;

public class Http2Trailers extends Http2Headers implements StreamMsg {

	public Http2Trailers() {
	}
	
	public Http2Trailers(List<Http2Header> headers) {
		super(headers);
	}

	public boolean isEndOfStream() {
		return true;
	}
	
	@Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.TRAILING_HEADERS;
	}
}
