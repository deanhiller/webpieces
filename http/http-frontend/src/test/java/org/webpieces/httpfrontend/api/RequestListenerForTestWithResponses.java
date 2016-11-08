package org.webpieces.httpfrontend.api;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.httpcommon.Responses;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

class RequestListenerForTestWithResponses extends RequestListenerForTest {
    private final boolean sendChunked;
    private final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    List<HttpResponse> responseList;
    Map<RequestId, HttpRequest> requestMap = new HashMap<>();

    RequestListenerForTestWithResponses(HttpResponse response, boolean sendChunked) {
        this.sendChunked = sendChunked;
        responseList = new ArrayList<>();
        responseList.add(response);
    }

    RequestListenerForTestWithResponses(List<HttpResponse> responseList, boolean sendChunked) {
        this.responseList = responseList;
        this.sendChunked = sendChunked;
    }

    private void sendResponse(HttpResponse response, HttpRequest req, RequestId requestId, ResponseSender sender) {
        if(!sendChunked) {
            sender.sendResponse(response, req, requestId, true);
        } else {
            // Strip body from response, then send it using sendData.

            HttpResponse newResponse = Responses.copyResponseExceptBody(response);
            DataWrapper data = response.getBodyNonNull();
            newResponse.setBody(dataGen.emptyWrapper());
            sender.sendResponse(newResponse, req, requestId, false).thenAccept(responseId -> sender.sendData(data, responseId, true));
        }
    }

    @Override
    public void incomingRequest(HttpRequest req, RequestId requestId, boolean isComplete, ResponseSender sender) {
        super.incomingRequest(req, requestId, isComplete, sender);
        requestMap.put(requestId, req);

        if(isComplete) {
            for(HttpResponse response: responseList) {
                sendResponse(response, req, requestId, sender);
            }
        }
    }

    @Override
    public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isComplete, ResponseSender sender) {
        return super.incomingData(data, id, isComplete, sender).thenAccept(v -> {
            if(isComplete)
                for(HttpResponse response: responseList) {
                    sendResponse(response, requestMap.get(id), id, sender);
                }
        });
    }
}
