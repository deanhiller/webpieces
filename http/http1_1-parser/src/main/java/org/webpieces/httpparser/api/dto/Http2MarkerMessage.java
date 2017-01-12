package org.webpieces.httpparser.api.dto;

public class Http2MarkerMessage extends HttpMessage {

	@Override
	public HttpMessageType getMessageType() {
		return HttpMessageType.HTTP2_MARKER_MSG;
	}

}
