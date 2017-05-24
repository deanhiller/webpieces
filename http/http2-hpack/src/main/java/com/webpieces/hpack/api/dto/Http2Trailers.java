package com.webpieces.hpack.api.dto;

import java.util.List;

import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class Http2Trailers extends Http2Headers implements PartialStream {

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
