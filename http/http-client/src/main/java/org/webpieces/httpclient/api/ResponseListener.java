package org.webpieces.httpclient.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import java.util.concurrent.CompletableFuture;

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
	 * incomingData will be called over and over until the last chunk in which case isLastChunk
	 * will be set to true and the chunk will be of size 0, but the last chunk is special in that
	 * it can contain extra headers with it.  
	 * 
	 * NOTE: All HttpChunks can contain extensions as well.  Those are dropped.
	 *
	 * For Http 2, the server could send back a PUSH_PROMISE which has an imputed request, so we
	 * need to pass back the imputed request from the PUSH_PROMISE message. For http1.1 responses
	 * and http2 'normal' responses, we just pass back the request that we started with here.
	 *
	 * For Http2, only 3 and 4 are possible, so 'incomingResponse' will be called once the header
	 * has arrived, and incomingData will be called for each dataframe that follows. If there is no
	 * data after the header (eg a HEAD request) then incomingResponse may be called with isComplete,
	 * but it's possible that an empty dataframe will be sent, in which case incomignData will be
	 * called with an empty dataWrapper and isLastData set to true.
	 *
	 * To support HTTP2 we need to pass the 'request' back with the response because the
	 * server might push a response with an implied request.
	 *
	 * @param resp The HttpResponse message including body if there is one
	 * @param req the originating or presumed request
	 * @param id an id to help us map incomingResponses to incomingDatas. UNUSED in HTTP1.1. In
	 *           HTTP1.1 you just have to take them serially-- ie all incomingdatas that show up
	 *           between incomingresponses belong to the prior incomingresponse.
	 * @param isComplete false if the transfer encoding is chunked or http/2 in which case
	 *                      incomingData will be called for each chunk/dataframe coming in
	 */
	void incomingResponse(HttpResponse resp, HttpRequest req, RequestId id, boolean isComplete);
	
	/**
	 *
	 * incomingData returns a future because we want to be able to signal that we're
	 * done processing this data and the flow control window can be opened back up again.
	 *
	 * @param data
	 * @param isLastData
	 */
	// maybe add optional chunk or chunk extension, or not
	CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isLastData);

	void failure(Throwable e);
	
}
