package com.webyoso.httpparser.api;

public class HttpUri {

	private String uri;

	public HttpUri(String uri) {
		this.uri = uri;
	}

	public String getUri() {
		return uri;
	}

	@Override
	public String toString() {
		return "" + uri;
	}
	
}
