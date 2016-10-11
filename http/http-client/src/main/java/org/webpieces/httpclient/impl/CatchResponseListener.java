package org.webpieces.httpclient.impl;

import org.webpieces.httpclient.api.RequestId;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

import java.util.concurrent.CompletableFuture;

public class CatchResponseListener implements ResponseListener {

	private static final Logger log = LoggerFactory.getLogger(CatchResponseListener.class);
	
	private ResponseListener listener;

	public CatchResponseListener(ResponseListener listener) {
		this.listener = listener;
	}

	@Override
	public void incomingResponse(HttpResponse resp, HttpRequest req, RequestId id, boolean isComplete) {
		try {
			listener.incomingResponse(resp, req, id, isComplete);
		} catch(Throwable e) {
			log.error("exception", e);
		}
	}

	@Override
	public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isLastData) {
		return listener.incomingData(data, id, isLastData).exceptionally(e -> {
			log.error("exception", e);
			return null;
		});
	}

	@Override
	public void failure(Throwable e) {
		try {
			listener.failure(e);
		} catch(Throwable ee) {
			log.error("exception", ee);
		}			
	}

}
