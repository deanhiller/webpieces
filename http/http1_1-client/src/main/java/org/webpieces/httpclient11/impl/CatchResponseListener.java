package org.webpieces.httpclient11.impl;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpStreamRef;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class CatchResponseListener implements HttpResponseListener {

	private static final Logger log = LoggerFactory.getLogger(CatchResponseListener.class);
	
	private HttpResponseListener listener;

	public CatchResponseListener(HttpResponseListener listener) {
		this.listener = listener;
	}

	@Override
	public HttpStreamRef incomingResponse(HttpResponse resp, boolean isComplete) {
		try {
			
			HttpStreamRef ref = listener.incomingResponse(resp, isComplete);
			
			CompletableFuture<HttpDataWriter> newWriter = ref.getWriter().thenApply(w -> new CatchDataWriter(w));
			return new ProxyStreamRef(ref, newWriter);
		} catch(Throwable e) {
			log.error("exception", e);
			CompletableFuture<HttpDataWriter> future = new CompletableFuture<HttpDataWriter>();
			future.completeExceptionally(e);
			return new ProxyStreamRef(null, future);
		}
	}

	private class CatchDataWriter implements HttpDataWriter {
		private HttpDataWriter writer;
		public CatchDataWriter(HttpDataWriter writer) {
			this.writer = writer;
		}

		@Override
		public CompletableFuture<Void> send(HttpData chunk) {
			try {
				return writer.send(chunk);
			} catch(Throwable e) {
				log.error("exception", e);
				CompletableFuture<Void> future = new CompletableFuture<Void>();
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
