package org.webpieces.http2client.integ;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class IntegColoradoEdu {

	private static final Logger log = LoggerFactory.getLogger(IntegColoradoEdu.class);
	
	public static void main(String[] args) throws InterruptedException {
		boolean isHttp = true;
		
		String host = "www.colorado.edu";
		int port = 443;
		if(isHttp)
			port = 80;
		
		Http2Headers req = createRequest(host);

		log.info("starting socket");
		ChunkedResponseListener listener = new ChunkedResponseListener();
		
		InetSocketAddress addr = new InetSocketAddress(host, port);
		Http2Socket socket = IntegSingleRequest.createHttpClient("oneTimerHttp2Socket", isHttp, addr);
		
		socket
			.connect(addr)
			.thenAccept(socet -> socket.send(req, listener))
			.exceptionally(e -> reportException(socket, e));

		Thread.sleep(100000);
	}

	private static Void reportException(Http2Socket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}
	
	private static class ChunkedResponseListener implements Http2ResponseListener, PushPromiseListener {

		@Override
		public CompletableFuture<Void> incomingPartialResponse(PartialStream response) {
			log.info("incoming part of response="+response);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public PushPromiseListener newIncomingPush(int streamId) {
			return this;
		}
		

		@Override
		public CompletableFuture<Void> incomingPushPromise(PartialStream response) {
			log.info("incoming push promise");
			return CompletableFuture.completedFuture(null);
		}

	}
	
	private static Http2Headers createRequest(String host) {
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
