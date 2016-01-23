package com.webyoso.httpparser.api;

public class RequestLine {
	private HttpMethod method;
	private HttpVersion version;
	
	public HttpMethod getMethod() {
		return method;
	}

	public void setMethod(HttpMethod method) {
		this.method = method;
	}

	public HttpVersion getVersion() {
		return version;
	}

	public void setVersion(HttpVersion version) {
		this.version = version;
	}
}
