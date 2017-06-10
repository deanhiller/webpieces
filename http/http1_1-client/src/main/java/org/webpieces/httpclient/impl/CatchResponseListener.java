package org.webpieces.httpclient.impl;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.DataWriter;
import org.webpieces.httpclient.api.HttpResponseListener;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class CatchResponseListener implements HttpResponseListener {

	private static final Logger log = LoggerFactory.getLogger(CatchResponseListener.class);
	
	private HttpResponseListener listener;

	public CatchResponseListener(HttpResponseListener listener) {
		this.listener = listener;
	}

	@Override
	public CompletableFuture<DataWriter> incomingResponse(HttpResponse resp, boolean isComplete) {
		try {
			return listener.incomingResponse(resp, isComplete)
					.thenApply(w -> new CatchDataWriter(w));
		} catch(Throwable e) {
			log.error("exception", e);
			CompletableFuture<DataWriter> future = new CompletableFuture<DataWriter>();
			future.completeExceptionally(e);
			return future;
		}
	}

	private class CatchDataWriter implements DataWriter {
		private DataWriter writer;
		public CatchDataWriter(DataWriter writer) {
			this.writer = writer;
		}

		@Override
		public CompletableFuture<DataWriter> incomingData(HttpData chunk) {
			try {
				return writer.incomingData(chunk).thenApply(w -> this);
			} catch(Throwable e) {
				log.error("exception", e);
				CompletableFuture<DataWriter> future = new CompletableFuture<DataWriter>();
				future.completeExceptionally(e);
				return future;
			}
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
