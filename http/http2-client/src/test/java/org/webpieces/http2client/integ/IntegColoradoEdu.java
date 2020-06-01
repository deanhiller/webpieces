package org.webpieces.http2client.integ;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.impl.Proxy2StreamRef;

import com.webpieces.hpack.api.dto.Http2Push;
import com.webpieces.hpack.api.dto.Http2Request;
import com.webpieces.hpack.api.dto.Http2Response;
import com.webpieces.http2engine.api.PushPromiseListener;
import com.webpieces.http2engine.api.PushStreamHandle;
import com.webpieces.http2engine.api.ResponseStreamHandle;
import com.webpieces.http2engine.api.StreamRef;
import com.webpieces.http2engine.api.StreamWriter;
import com.webpieces.http2parser.api.dto.CancelReason;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

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
			.thenAccept(socet -> socket.openStream().process(req, listener))
			.exceptionally(e -> reportException(socket, e));

		Thread.sleep(100000);
	}

	private static Void reportException(Http2Socket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}
	
	private static class ChunkedResponseListener implements ResponseStreamHandle, PushPromiseListener, PushStreamHandle {
		@Override
		public StreamRef process(Http2Response response) {
			log.info("incoming part of response="+response);
			CompletableFuture<StreamWriter> writer = CompletableFuture.completedFuture(null);
			
			return new Proxy2StreamRef(null, writer);
		}

		@Override
		public PushStreamHandle openPushStream() {
			return this;
		}
		
		@Override
		public CompletableFuture<StreamWriter> processPushResponse(Http2Response response) {
			log.info("incoming push promise. response="+response);
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
		
		Http2Request req = new Http2Request();
		req.addHeader(new Http2Header(Http2HeaderName.METHOD, "GET"));
		req.addHeader(new Http2Header(Http2HeaderName.PATH, "GET"));
		req.addHeader(new Http2Header(Http2HeaderName.SCHEME, "http"));
		req.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, "www.colorado.edu"));
		
		req.addHeader(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/0.9"));
		req.addHeader(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
		
		return req;
	}
}
