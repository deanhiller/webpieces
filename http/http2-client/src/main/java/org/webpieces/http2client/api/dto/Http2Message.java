package org.webpieces.http2client.api.dto;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2.api.dto.highlevel.Http2Trailers;

public abstract class Http2Message {

	protected DataWrapper payload;
	protected Http2Trailers trailingHeaders;
	
	public Http2Message() {}

	public Http2Message(DataWrapper payload, Http2Trailers trailingHeaders) {
		super();
		this.payload = payload;
		this.trailingHeaders = trailingHeaders;
	}

	public DataWrapper getPayload() {
		return payload;
	}

	public void setPayload(DataWrapper payload) {
		this.payload = payload;
	}

	public Http2Trailers getTrailingHeaders() {
		return trailingHeaders;
	}

	public void setTrailingHeaders(Http2Trailers trailingHeaders) {
		this.trailingHeaders = trailingHeaders;
	}

}
