package org.webpieces.httpclient.api;

import org.webpieces.httpparser.api.dto.HttpChunk;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

public interface ResponseListener {

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
	 * For Http 2, the server could send back a PUSH_PROMISE which has an imputed request, so we
	 * need to pass back the imputed request from the PUSH_PROMISE message. For http1.1 responses
	 * and http2 'normal' responses, we just pass back the request that we started with here.
	 *
	 * For http2 we don't need to worry about chunking.
	 * 
	 * @param resp The HttpResponse message including body if there is one
	 * @param isComplete false if the transfer encoding is chunked in which case incomingChunk will
	 * be called for each chunk coming
	 */
	public void incomingResponse(HttpResponse resp, boolean isComplete);

	/**
	 * To support HTTP2 we need to pass the 'request' back with the response because the
	 * server might push a response with an implied request.
	 */
	public void incomingResponse(HttpResponse resp, HttpRequest req, boolean isComplete);
	
	/**
	 * 
	 * @param chunk
	 * @param isLastChunk
	 */
	public void incomingChunk(HttpChunk chunk, boolean isLastChunk);
	
	public void failure(Throwable e);
	
}
