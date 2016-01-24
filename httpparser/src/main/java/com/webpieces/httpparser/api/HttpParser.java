package com.webpieces.httpparser.api;

import com.webpieces.httpparser.api.dto.HttpRequest;

public interface HttpParser {

	public byte[] marshalToBytes(HttpRequest request);
	
	public String marshalToString(HttpRequest request);

}
