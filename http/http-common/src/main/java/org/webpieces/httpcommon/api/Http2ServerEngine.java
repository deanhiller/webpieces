package org.webpieces.httpcommon.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import java.util.concurrent.CompletableFuture;

public interface Http2ServerEngine extends Http2Engine {
    // Can be used to initiate multiple responses to the same requestid, but the 'request' that comes back
    // for the second and future response are different from the first response, they are the 'assumed request'
    // that this 'push' response is associated with.
    CompletableFuture<ResponseId> sendResponse(HttpResponse response, HttpRequest request, RequestId requestId, boolean isComplete);
    CompletableFuture<Void> sendData(DataWrapper data, ResponseId id, boolean isComplete);
    ResponseSender getResponseSender();
    void setRequestListener(RequestListener requestListener);
}
