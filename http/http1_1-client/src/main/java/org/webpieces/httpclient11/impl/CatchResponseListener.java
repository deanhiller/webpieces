package org.webpieces.httpclient11.impl;

import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpResponse;

public class CatchResponseListener implements HttpResponseListener {

	private static final Logger log = LoggerFactory.getLogger(CatchResponseListener.class);
	
	private HttpResponseListener listener;

	private String svrSocket;

	public CatchResponseListener(HttpResponseListener listener, String svrSocket) {
		this.listener = listener;
		this.svrSocket = svrSocket;
	}

	@Override
	public CompletableFuture<HttpDataWriter> incomingResponse(HttpResponse resp, boolean isComplete) {
		try {
			MDC.put("svrSocket", svrSocket);
			return listener.incomingResponse(resp, isComplete)
					.thenApply(w -> new CatchDataWriter(w, svrSocket));
		} catch(Throwable e) {
			log.error("exception", e);
			CompletableFuture<HttpDataWriter> future = new CompletableFuture<HttpDataWriter>();
			future.completeExceptionally(e);
			return future;
		} finally {
			MDC.put("svrSocket", null);
		}
	}

	private class CatchDataWriter implements HttpDataWriter {
		private HttpDataWriter writer;
		private String svrSocket2;
		public CatchDataWriter(HttpDataWriter writer, String svrSocket) {
			this.writer = writer;
			svrSocket2 = svrSocket;
		}

		@Override
		public CompletableFuture<Void> send(HttpData chunk) {
			try {
				MDC.put("svrSocket", svrSocket2);
				return writer.send(chunk);
			} catch(Throwable e) {
				log.error("exception", e);
				CompletableFuture<Void> future = new CompletableFuture<Void>();
				future.completeExceptionally(e);
				return future;
			} finally {
				MDC.put("svrSocket", null);
			}
		}
	}
	
	@Override
	public void failure(Throwable e) {
		try {
			MDC.put("svrSocket", svrSocket);			
			listener.failure(e);
		} catch(Throwable ee) {
			log.error("exception", ee);
		} finally {
			MDC.put("svrSocket", null);			
		}
	}

//	@Override
//	public void socketClosed() {
//		try {
//			MDC.put("svrSocket", svrSocket);
//			log.info("Far end closed the client socket.");
//			listener.socketClosed();
//		} catch(Throwable e) {
//			log.error("Exception closing", e);
//		} finally {
//			MDC.put("svrSocket", null);						
//		}
//	}

}
