package org.webpieces.httpproxy.impl.responsechain;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.frontend.api.FrontendSocket;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import java.util.concurrent.CompletableFuture;

public class Layer1Response implements ResponseListener {

	private Layer2ResponseListener responseListener;
	private FrontendSocket channel;
	private HttpRequest req;

	public Layer1Response(Layer2ResponseListener responseListener, FrontendSocket channel, HttpRequest req) {
		this.responseListener = responseListener;
		this.channel = channel;
		this.req = req;
	}

	@Override
	public void incomingResponse(HttpResponse resp, boolean isComplete) {
		responseListener.processResponse(channel, req, resp, isComplete);
	}

	@Override
	public void incomingResponse(HttpResponse resp, HttpRequest req, boolean isComplete) {
		incomingResponse(resp, isComplete);
	}

	@Override
	public CompletableFuture<Integer> incomingData(DataWrapper data, boolean isLastData) {
		// TODO: fix this to deal with the fact that we are getting only the datawrapper now
		// not the chunk.
		//responseListener.processResponse(channel, req, data, isLastData);
		return CompletableFuture.completedFuture(data.getReadableSize());
	}

    @Override
    public CompletableFuture<Integer> incomingData(DataWrapper data, HttpRequest request, boolean isLastData) {
		return incomingData(data, isLastData);
    }

    @Override
	public void failure(Throwable e) {
		responseListener.processError(channel, req, e);
	}

}
