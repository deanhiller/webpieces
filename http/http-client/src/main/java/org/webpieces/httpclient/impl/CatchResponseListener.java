package org.webpieces.httpclient.impl;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2parser.api.dto.lib.Http2Header;

public class CatchResponseListener implements ResponseListener {

	private static final Logger log = LoggerFactory.getLogger(CatchResponseListener.class);
	
	private ResponseListener listener;

	public CatchResponseListener(ResponseListener listener) {
		this.listener = listener;
	}

	@Override
	public void incomingTrailer(List<Http2Header> headers, ResponseId id, boolean isComplete) {
		try {
			listener.incomingTrailer(headers, id, isComplete);
		} catch(Throwable e) {
			log.error("exception", e);
		}
	}

	@Override
	public void incomingResponse(HttpResponse resp, HttpRequest req, ResponseId id, boolean isComplete) {
		try {
			listener.incomingResponse(resp, req, id, isComplete);
		} catch(Throwable e) {
			log.error("exception", e);
		}
	}

	@Override
	public CompletableFuture<Void> incomingData(DataWrapper data, ResponseId id, boolean isLastData) {
		try {
			return listener.incomingData(data, id, isLastData).exceptionally(e -> {
				log.error("exception", e);
				return null;
			});
		} catch(Throwable e) {
			log.error("exception", e);
			return CompletableFuture.completedFuture(null);
		}
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
