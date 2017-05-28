package org.webpieces.httpparser.api.dto;

public enum HttpMessageType {


	REQUEST,
	RESPONSE, 
	
	//For http1.1, chunks are given when doing chunking with a last chunk when it is done of 0 bytes per spec
	CHUNK,
	LAST_CHUNK,

	//This is raw data payloads when there is a chunking is not on and Content-Length was supplied
	//except we give it to you in pieces so you can stream it(it also matches http2 better)
	DATA,
	
	//A marker message for http2 so when we parse, this is returned to identify http2
	HTTP2_MARKER_MSG,
}
