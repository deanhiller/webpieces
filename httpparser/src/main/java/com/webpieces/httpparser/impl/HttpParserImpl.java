package com.webpieces.httpparser.impl;

import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.ParsedData;
import com.webpieces.httpparser.api.ParsedStatus;
import com.webpieces.httpparser.api.dto.HttpMessage;
import com.webpieces.httpparser.api.dto.HttpMessageType;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpRequestLine;
import com.webpieces.httpparser.api.dto.HttpResponse;
import com.webpieces.httpparser.api.dto.HttpResponseStatus;
import com.webpieces.httpparser.api.dto.HttpResponseStatusLine;

public class HttpParserImpl implements HttpParser {

	@Override
	public byte[] marshalToBytes(HttpMessage request) {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public String marshalToString(HttpMessage httpMsg) {
		if(httpMsg.getMessageType() == HttpMessageType.REQUEST)
			validate(httpMsg.getHttpRequest());
		else if(httpMsg.getMessageType() == HttpMessageType.RESPONSE)
			validate(httpMsg.getHttpResponse());
		
		//TODO: perhaps optimize and use StringBuilder on the Header for loop
		//Java optimizes most to StringBuilder but for a for loop, it doesn't all the time...
		StringBuilder builder = new StringBuilder();
		builder.append(httpMsg + "");
		return builder.toString();
	}

	private void validate(HttpResponse response) {
		HttpResponseStatusLine statusLine = response.getStatusLine();
		if(statusLine == null) {
			throw new IllegalArgumentException("response.statusLine is not set(call response.setStatusLine");
		}
		HttpResponseStatus status = statusLine.getStatus();
		if(status == null) {
			throw new IllegalArgumentException("response.statusLine.status is not set(call response.getStatusLine().setStatus())");
		} else if(status.getCode() == null) {
			throw new IllegalArgumentException("response.statusLine.status.code is not set(call response.getStatusLine().getStatus().setCode())");
		} else if(status.getReason() == null) {
			throw new IllegalArgumentException("response.statusLine.status.reason is not set");
		} else if(statusLine.getVersion() == null) {
			throw new IllegalArgumentException("response.statusLine.version is not set");
		}
		
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

	@Override
	public ParsedData unmarshalAsync(byte[] msg) {
		return null;
	}

	@Override
	public HttpMessage unmarshal(byte[] msg) {
		ParsedData parsedData = unmarshalAsync(msg);
		if(parsedData.getStatus() == ParsedStatus.MSG_PARSED_AND_LEFTOVER_DATA)
			throw new IllegalArgumentException("There is more data than one http message.  Use unmarshalAsync instead");
		else if(parsedData.getStatus() == ParsedStatus.NOT_ENOUGH_DATA)
			throw new IllegalArgumentException("This http message is not complete.  Use unmarshalAsynch instead or "
					+ "fix client code to pass in complete http message(or report a bug if it is this libraries fault)");
		
		return parsedData.getMsg();
	}
}
