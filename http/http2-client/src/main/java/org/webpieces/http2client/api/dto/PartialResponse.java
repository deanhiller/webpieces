package org.webpieces.http2client.api.dto;

public interface PartialResponse {

	boolean isLastPartOfResponse();
	
	int getStreamId();
}
