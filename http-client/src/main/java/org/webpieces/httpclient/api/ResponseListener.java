package org.webpieces.httpclient.api;

import com.webpieces.httpparser.api.dto.HttpChunk;
import com.webpieces.httpparser.api.dto.HttpResponse;

public interface ResponseListener {

	/**
	 * @param resp The HttpResponse message including body if there is one
	 * @param isComplete false if the transfer encoding is chunked in which case incomingChunk will
	 * be called for each chunk coming
	 */
	public void incomingResponse(HttpResponse resp, boolean isComplete);
	
	/**
	 * 
	 * @param chunk
	 * @param isLastChunk
	 */
	public void incomingChunk(HttpChunk chunk, boolean isLastChunk);
	
}
