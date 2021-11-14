package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.http2client.api.Http2Socket;

import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.streaming.PushPromiseListener;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;

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
		public XFuture<StreamWriter> process(Http2Response response) {
			log.info("incoming part of response="+response);
			return XFuture.completedFuture(null);
		}

		@Override
		public PushStreamHandle openPushStream() {
			return this;
		}
		
		@Override
		public XFuture<StreamWriter> processPushResponse(Http2Response response) {
			log.info("incoming push promise. response="+response);
			return XFuture.completedFuture(null);
		}

		@Override
		public XFuture<Void> cancel(CancelReason frame) {
			return XFuture.completedFuture(null);
		}

		@Override
		public XFuture<Void> cancelPush(CancelReason payload) {
			return XFuture.completedFuture(null);
		}
		@Override
		public XFuture<PushPromiseListener> process(Http2Push headers) {
			return XFuture.completedFuture(this);
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
