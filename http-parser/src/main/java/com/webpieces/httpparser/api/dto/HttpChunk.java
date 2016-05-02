package com.webpieces.httpparser.api.dto;

import java.util.ArrayList;
import java.util.List;

public class HttpChunk extends HttpMessage {

	private List<String> extensions = new ArrayList<>();
	
	@Override
	public HttpMessageType getMessageType() {
		return HttpMessageType.CHUNK;
	}

	public void addExtension(String extension) {
		extensions.add(extension);
	}

}
