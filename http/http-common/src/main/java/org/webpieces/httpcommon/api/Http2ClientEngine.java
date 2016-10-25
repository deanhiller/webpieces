package org.webpieces.httpcommon.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import java.util.concurrent.CompletableFuture;

public interface Http2ClientEngine extends Http2Engine {
    // These are both needed to give the RequestSender the ability to deal with negotiating http1.1 vs http2.
    RequestId createInitialStream(HttpResponse r, HttpRequest req, ResponseListener listener, DataWrapper leftOverData);
    void sendHttp2Preface();

    CompletableFuture<RequestId> sendRequest(HttpRequest request, boolean isComplete, ResponseListener l);
    CompletableFuture<Void> sendData(RequestId id, DataWrapper data, boolean isComplete);
}
