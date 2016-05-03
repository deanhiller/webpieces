package org.webpieces.httpclient.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.ResponseListener;

import com.webpieces.httpparser.api.dto.HttpChunk;
import com.webpieces.httpparser.api.dto.HttpResponse;

public class CatchResponseListener implements ResponseListener {

	private static final Logger log = LoggerFactory.getLogger(CatchResponseListener.class);
	
	private ResponseListener listener;

	public CatchResponseListener(ResponseListener listener) {
		this.listener = listener;
	}

	@Override
	public void incomingResponse(HttpResponse resp, boolean isComplete) {
		try {
			listener.incomingResponse(resp, isComplete);
		} catch(Throwable e) {
			log.warn("exception", e);
		}
	}

	@Override
	public void incomingChunk(HttpChunk chunk, boolean isLastChunk) {
		try {
			listener.incomingChunk(chunk, isLastChunk);
		} catch(Throwable e) {
			log.warn("exception", e);
		}
	}

	@Override
	public void failure(Throwable e) {
		try {
			listener.failure(e);
		} catch(Throwable ee) {
			log.warn("exception", ee);
		}			
	}

}
