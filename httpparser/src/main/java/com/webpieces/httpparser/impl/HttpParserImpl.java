package com.webpieces.httpparser.impl;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.RequestLine;

public class HttpParserImpl implements HttpParser {

	@Override
	public byte[] marshalToBytes(HttpRequest request) {
		return null;
	}

	@Override
	public String marshalToString(HttpRequest request) {
		validate(request);
		StringBuilder builder = new StringBuilder();
		
		builder.append(request.getRequestLine()+"");
		
		return builder.toString();
	}

	private void validate(HttpRequest request) {
		RequestLine requestLine = request.getRequestLine();
		if(requestLine == null) {
			throw new IllegalArgumentException("request.requestLine is not set(call request.setRequestLine()");
		} else if(requestLine.getMethod() == null) {
			throw new IllegalArgumentException("request.requestLine.method is not set(call request.getRequestLine().setMethod()");
		} else if(requestLine.getVersion() == null) {
			throw new IllegalArgumentException("request.requestLine.version is not set(call request.getRequestLine().setVersion()");
		}
		
		
	}

}
