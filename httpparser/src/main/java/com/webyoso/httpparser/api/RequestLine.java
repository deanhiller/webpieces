package com.webyoso.httpparser.api;

public class RequestLine {
	private HttpUri uri;
	private HttpMethod method;
	private HttpVersion version = new HttpVersion();
	
	public HttpUri getUri() {
		return uri;
	}

	public void setUri(HttpUri httpUri) {
		this.uri = httpUri;
	}

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

	@Override
	public String toString() {
		return method + " " +  uri + " " + version + "\r\n";
	}
}
