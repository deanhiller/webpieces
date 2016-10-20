package org.webpieces.httpfrontend.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MockRequestListenerWithResponse extends MockRequestListener {
    HttpResponse response;
    Map<RequestId, HttpRequest> requestMap = new HashMap<>();

    public MockRequestListenerWithResponse(HttpResponse response) {
        this.response = response;
    }

    @Override
    public void incomingRequest(HttpRequest req, RequestId requestId, boolean isComplete, ResponseSender sender) {
        super.incomingRequest(req, requestId, isComplete, sender);
        requestMap.put(requestId, req);

        if(isComplete)
            sender.sendResponse(response, req, requestId, true);
    }

    @Override
    public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender) {
        return super.incomingData(data, id, isComplete, sender).thenAccept(v -> {
            if(isComplete)
                sender.sendResponse(response, requestMap.get(id), id, true);
        });
    }
}
