package com.webyoso.httpparser.api;

public class HttpRequest {

	private RequestLine requestLine;

	public RequestLine getRequestLine() {
		return requestLine;
	}

	public void setRequestLine(RequestLine requestLine) {
		this.requestLine = requestLine;
	}

	@Override
	public String toString() {
		return "" + requestLine;
	}

}
