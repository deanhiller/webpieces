package com.webpieces.http2parser.api.highlevel;

public interface ToClient {
	
	void incomingPayload(Http2Payload frame);
	
}
