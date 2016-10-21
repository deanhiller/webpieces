package org.webpieces.httpfrontend.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class MockRequestListenerWithResponses extends MockRequestListener {
    List<HttpResponse> responseList;
    Map<RequestId, HttpRequest> requestMap = new HashMap<>();

    MockRequestListenerWithResponses(HttpResponse response) {
        responseList = new ArrayList<>();
        responseList.add(response);
    }

    public MockRequestListenerWithResponses(List<HttpResponse> responseList) {
        this.responseList = responseList;
    }

    @Override
    public void incomingRequest(HttpRequest req, RequestId requestId, boolean isComplete, ResponseSender sender) {
        super.incomingRequest(req, requestId, isComplete, sender);
        requestMap.put(requestId, req);

        if(isComplete) {
            for(HttpResponse response: responseList) {
                sender.sendResponse(response, req, requestId, true);
            }
        }
    }

    @Override
    public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender) {
        return super.incomingData(data, id, isComplete, sender).thenAccept(v -> {
            if(isComplete)
                for(HttpResponse response: responseList) {
                    sender.sendResponse(response, requestMap.get(id), id, true);
                }
        });
    }
}
