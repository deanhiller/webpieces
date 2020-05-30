package com.webpieces.hpack.api.dto;

import java.util.List;

import com.webpieces.http2parser.api.dto.StatusCode;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.Http2MsgType;

public class Http2Response extends Http2Headers {

	public Http2Response() {
	}
	
	public Http2Response(List<Http2Header> headers) {
		super(headers);
	}

	@Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.RESPONSE_HEADERS;
	}

	public StatusCode getKnownStatusCode() {
		return StatusCode.lookup(getStatus());
	}
	
	public Integer getStatus() {
		String statusVal = getSingleHeaderValue(Http2HeaderName.STATUS);
		if(statusVal == null)
			return null;
		return Integer.parseInt(statusVal);
	}

	public boolean isStatusSet() {
		return getStatus() != null;
	}


}
