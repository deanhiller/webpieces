package com.webpieces.http2.api.dto.highlevel;

import java.util.List;

import com.webpieces.http2.api.dto.lowlevel.Http2Method;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2MsgType;

public class Http2Request extends Http2Headers {

	public Http2Request() {
	}
	
	public Http2Request(List<Http2Header> headers) {
		super(headers);
	}

	@Override
	public Http2MsgType getMessageType() {
		return Http2MsgType.REQUEST_HEADERS;
	}

	public Http2Method getKnownMethod() {
		String methodString = getMethodString();
		if(methodString == null)
			return null;
		return Http2Method.lookup(methodString);
	}

	public String getUrl() {
		return getScheme()+"://"+getAuthority()+"/"+getPath();
	}

	public String getMethodString() {
		return getSingleHeaderValue(Http2HeaderName.METHOD);
	}

	public String getScheme() { return getSingleHeaderValue(Http2HeaderName.SCHEME); }

	public String getAuthority() {
		return getSingleHeaderValue(Http2HeaderName.AUTHORITY);
	}

	public String getPath() {
		return getSingleHeaderValue(Http2HeaderName.PATH);
	}
	
}
