package com.webpieces.httpparser.impl;

import java.util.List;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.common.Header;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpRequestLine;

public class HttpParserImpl implements HttpParser {

	@Override
	public byte[] marshalToBytes(HttpRequest request) {
		return null;
	}

	@Override
	public String marshalToString(HttpRequest request) {
		validate(request);
		
		//TODO: perhaps optimize and use StringBuilder on the Header for loop
		//Java optimizes most to StringBuilder but for a for loop, it doesn't all the time...
		StringBuilder builder = new StringBuilder();
		builder.append(request + "");
		return builder.toString();
	}

	private void validate(HttpRequest request) {
		HttpRequestLine requestLine = request.getRequestLine();
		if(requestLine == null) {
			throw new IllegalArgumentException("request.requestLine is not set(call request.setRequestLine()");
		} else if(requestLine.getMethod() == null) {
			throw new IllegalArgumentException("request.requestLine.method is not set(call request.getRequestLine().setMethod()");
		} else if(requestLine.getVersion() == null) {
			throw new IllegalArgumentException("request.requestLine.version is not set(call request.getRequestLine().setVersion()");
		}
	}

}
