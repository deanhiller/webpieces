package org.webpieces.httpclient.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpRequest;
import java.util.concurrent.CompletableFuture;

/**
 * The RequestListener and ResponseListener are the key interfaces used to interact
 * with an http client connected to a server, or an
 * http server.
 *
 * HTTP server side applications talk to
 * the HTTP server by providing a RequestListener and interacting with the server's
 * ResponseListener.
 *
 * Client side applications talk to the HTTP client by providing a ResponseListener
 * and interacting with the client's RequestListener.
 *
 */
public interface RequestListener {
    // Change this to be more user-friendly /sender not listener
    /**
     *
     * Initiate an HTTP request. Returns an ID so that the client can send more data
     * along and reference this original request. If we are on http1.1
     * the request id comes back as 0, and we have to make sure that
     * we we send an isComplete (either from incomingRequest or incomingData) before
     * sending a new incomingRequest.
     *
     * @param request
     * @param isComplete
     * @param listener
     * @return
     */
    CompletableFuture<RequestId> incomingRequest(HttpRequest request, boolean isComplete, ResponseListener listener);

    /**
     *
     * Pass along additional data to the HTTP request referenced. Returns the side of the
     * datawrapper.
     *
     * @param data
     * @return
     */
    CompletableFuture<Integer> incomingData(RequestId id, DataWrapper data, boolean isComplete);

    void failure(Throwable e);
}
