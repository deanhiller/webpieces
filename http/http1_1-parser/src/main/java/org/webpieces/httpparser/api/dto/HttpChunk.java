package org.webpieces.httpparser.api.dto;

import org.webpieces.data.api.DataWrapper;

public class HttpChunk extends HttpData {
		
	public HttpChunk() {
	}
	public HttpChunk(DataWrapper data) {
		super(data, false);
	}

	@Override
	public boolean isEndOfData() {
		return false;
	}

	public boolean isStartOfChunk() {
		return true;
	}

	public boolean isEndOfChunk() {
		return true;
	}
	
	@Override
	public HttpMessageType getMessageType() {
		return HttpMessageType.CHUNK;
	}

}
