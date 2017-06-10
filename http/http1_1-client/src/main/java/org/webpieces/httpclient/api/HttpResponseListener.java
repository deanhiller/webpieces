package org.webpieces.httpclient.api;

import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpResponseListener {

	/**
	 * This is sort of a hard api issue to solve.  When you send a request out to an http server
	 * for http 1.1, the server can do one of 3 things
	 * 
	 * 1. send back a single response with no body
	 * 2. send back a response with a body (this body could be quite large)
	 * 3. send back a response, then send as many chunks as it wants followed by a last chunk(size=0)
	 * 4. send back a response, then infinitely stream chunks (some apis like twitter do this)
	 * 
	 * This makes the callback api have a larger surface area than desired.  The incomingResponse
	 * method will have isComplete=true in the cases of #1 or #2 above.  In the case of #3, 
	 * incomingChunk will be called over and over until the last chunk in which case isLastChunk
	 * will be set to true and the chunk will be of size 0, but the last chunk is special in that
	 * it can contain extra headers with it.  
	 * 
	 * NOTE: All HttpChunks can contain extensions as well.  Those are included if they exist in the
	 * HttpChunk object
	 * 
	 * @param resp The HttpResponse message including body if there is one
	 * @param isComplete false if the transfer encoding is chunked in which case incomingChunk will
	 * be called for each chunk coming
	 */
	public CompletableFuture<DataWriter> incomingResponse(HttpResponse resp, boolean isComplete);
	
	public void failure(Throwable e);

}
