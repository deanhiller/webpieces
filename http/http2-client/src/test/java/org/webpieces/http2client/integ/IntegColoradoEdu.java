package org.webpieces.http2client.integ;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseHandler2;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;

public class IntegColoradoEdu {

	private static final Logger log = LoggerFactory.getLogger(IntegColoradoEdu.class);
	
	public static void main(String[] args) throws InterruptedException {
		boolean isHttp = true;
		
		String host = "www.colorado.edu";
		int port = 443;
		if(isHttp)
			port = 80;
		
		Http2Request req = createRequest(host);

		log.info("starting socket");
		ChunkedResponseListener listener = new ChunkedResponseListener();
		
		InetSocketAddress addr = new InetSocketAddress(host, port);
		Http2Socket socket = IntegSingleRequest.createHttpClient("oneTimerHttp2Socket", isHttp, addr);
		
		socket
			.connect(addr)
			.thenAccept(socet -> socket.openStream(listener).process(req))
			.exceptionally(e -> reportException(socket, e));

		Thread.sleep(100000);
	}

	private static Void reportException(Http2Socket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}
	
	private static class ChunkedResponseListener implements ResponseHandler2, PushPromiseListener, PushStreamHandle {
		@Override
		public CompletableFuture<StreamWriter> process(Http2Response response) {
			log.info("incoming part of response="+response);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public PushStreamHandle openPushStream() {
			return this;
		}
		
		@Override
		public CompletableFuture<StreamWriter> incomingPushResponse(Http2Response response) {
			log.info("incoming push promise. response="+response);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Void> cancel(CancelReason frame) {
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public CompletableFuture<Void> cancelPush(CancelReason payload) {
			return CompletableFuture.completedFuture(null);
		}
		@Override
		public CompletableFuture<PushPromiseListener> process(Http2Push headers) {
			return CompletableFuture.completedFuture(this);
		}
	}
	
	private static Http2Request createRequest(String host) {
//		GET / HTTP/1.1
//		Host: www.colorado.edu
//		User-Agent: curl/7.43.0
//		Accept: */*
//		
//		HttpRequestLine requestLine = new HttpRequestLine();
//		requestLine.setMethod(KnownHttpMethod.GET);
//		requestLine.setUri(new HttpUri("/"));
//		
//		HttpRequest req = new HttpRequest();
//		req.setRequestLine(requestLine);
//		req.addHeader(new Header(KnownHeaderName.HOST, host));
//		req.addHeader(new Header(KnownHeaderName.ACCEPT, "*/*"));
//		req.addHeader(new Header(KnownHeaderName.USER_AGENT, "webpieces/0.9"));
		return null;
	}
}
