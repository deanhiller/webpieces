package org.webpieces.httpclient.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import java.util.concurrent.CompletableFuture;

/**
 * The RequestSender and ResponseListener are the key interfaces used to interact
 * with an HTTP client. The customer client application calls
 * the RequestSender to send requests and defines an ResponseListener to
 * handle requests.
 *
 * The RequestListener and ResponseProcessor are the key interfaces used to interact
 * with an HTTP server. The customer server application defines a RequestListener
 * and calls the ResponseProcessor.
 *
 * The RequestListener and RequestSender should be almost identical interfaces, except
 * for method names to make clearer what is happening.
 *
 * The ResponseListener and ResponseProcessor should be also almost identical interfaces,
 * except for method names to make clearer what is happening.
 *
 */
public interface RequestSender {
    /**
     * This can be used ONLY if 'you' know that the far end does NOT sended a chunked download.
     * The reason is in a chunked download, we don't want to blow up your RAM.  Some apis like
     * twitters streaming api and we would never ever be done and have a full response.  Others
     * are just a very very large download you don't want existing in RAM anyways.
     *
     * @param request
     */
    //TODO: Implement timeout for clients so that requests will timeout
    CompletableFuture<HttpResponse> send(HttpRequest request);

    /**
     *
     * Initiate an HTTP request. Returns an ID so that the client can send more data
     * along and reference this original request. If we are on http1.1
     * the request id comes back as 0, and we have to make sure that
     * we we send an isComplete (either from sendRequest or sendData) before
     * sending a new sendRequest.
     *
     * @param request
     * @param isComplete
     * @param listener
     * @return
     */
    CompletableFuture<RequestId> sendRequest(HttpRequest request, boolean isComplete, ResponseListener listener);

    /**
     *
     * Pass along additional data to the HTTP request referenced. Returns the side of the
     * datawrapper.
     *
     * @param data
     * @return
     */
    CompletableFuture<Integer> sendData(RequestId id, DataWrapper data, boolean isComplete);

    void failure(Throwable e);
}
