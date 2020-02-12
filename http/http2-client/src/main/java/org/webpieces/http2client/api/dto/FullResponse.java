package org.webpieces.http2client.api.dto;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.hpack.api.dto.Http2Trailers;

public class FullResponse extends Http2Message {

	private Http2Response headers;

	public FullResponse() {
	}
	
	public FullResponse(Http2Response headers, DataWrapper fullData, Http2Trailers trailingHeaders) {
		super(fullData, trailingHeaders);
		this.headers = headers;
	}

	public Http2Response getHeaders() {
		return headers;
	}

	public void setHeaders(Http2Response headers) {
		this.headers = headers;
	}

	@Override
	public String toString() {
		return "FullResponse[headers="+headers+" body size="+payload.getReadableSize()+" trailers="+trailingHeaders;
	}
}
