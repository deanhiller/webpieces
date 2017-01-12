package org.webpieces.httpparser.api.dto;

public enum HttpMessageType {

	CHUNK,
	REQUEST,
	RESPONSE, 
	LAST_CHUNK,
	HTTP2_MARKER_MSG
}
