package com.webpieces.httpparser.api;

import com.webpieces.httpparser.api.dto.HttpMessage;

public interface HttpParser {

	public byte[] marshalToBytes(HttpMessage request);
	
	public String marshalToString(HttpMessage request);

	/**
	 * A special method where you may give part of an HttpMessage or
	 * 1.5 HttpMessages
	 * 
	 * @param msg
	 * @return
	 */
	public ParsedData unmarshalAsync(byte[] msg);
	
	public HttpMessage unmarshal(byte[] msg);
}
