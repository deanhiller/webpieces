package org.webpieces.httpproxy.impl.responsechain;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.ResponseSender;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import java.util.concurrent.CompletableFuture;

public class Layer1Response implements ResponseListener {

	private Layer2ResponseListener responseListener;
	private ResponseSender responseSender;
	private HttpRequest req;

	public Layer1Response(Layer2ResponseListener responseListener, ResponseSender responseSender, HttpRequest req) {
		this.responseListener = responseListener;
		this.responseSender = responseSender;
		this.req = req;
	}

	@Override
	public void incomingResponse(HttpResponse resp, HttpRequest req, RequestId id, boolean isComplete) {
		responseListener.processResponse(responseSender, req, resp, id, isComplete);

	}

    @Override
    public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isLastData) {
		return responseListener.processData(responseSender, data, id, isLastData);
    }

    @Override
	public void failure(Throwable e) {
		responseListener.processError(responseSender, req, e);
	}

}
