package org.webpieces.http2client.api.dto;

import org.webpieces.data.api.DataWrapper;

import com.webpieces.http2engine.api.dto.Http2Headers;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

public abstract class Http2Message {

	protected Http2Headers headers = new Http2Headers();
	protected DataWrapper payload;
	protected Http2Headers trailingHeaders;
	
	public Http2Message() {}

	public Http2Message(Http2Headers headers, DataWrapper payload, Http2Headers trailingHeaders) {
		super();
		this.headers = headers;
		this.payload = payload;
		this.trailingHeaders = trailingHeaders;
	}

	public Http2Headers getHeaders() {
		return headers;
	}

	public void setHeaders(Http2Headers headers) {
		this.headers = headers;
	}

	public DataWrapper getPayload() {
		return payload;
	}

	public void setPayload(DataWrapper payload) {
		this.payload = payload;
	}

	public Http2Headers getTrailingHeaders() {
		return trailingHeaders;
	}

	public void setTrailingHeaders(Http2Headers trailingHeaders) {
		this.trailingHeaders = trailingHeaders;
	}

	public void addHeader(Http2Header header) {
		headers.addHeader(header);
	}
}
