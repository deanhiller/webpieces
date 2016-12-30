package org.webpieces.httpclient;

import java.net.InetSocketAddress;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.Http2ResponseListener;
import org.webpieces.httpclient.api.Http2ServerListener;
import org.webpieces.httpclient.api.Http2Socket;
import org.webpieces.httpclient.api.Http2SocketDataReader;
import org.webpieces.httpclient.api.dto.Http2Headers;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2parser.api.dto.Http2UnknownFrame;

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
		Http2Socket socket = IntegGoogleHttps.createHttpClient("oneTimerHttp2Socket", isHttp, addr);
		
		socket
			.connect(addr, new ServerListenerImpl())
			.thenAccept(socet -> socket.sendRequest(req, listener, true))
			.exceptionally(e -> reportException(socket, e));

		Thread.sleep(100000);
	}

	private static Void reportException(Http2Socket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}

	private static class ServerListenerImpl implements Http2ServerListener, Http2SocketDataReader {

		@Override
		public void farEndClosed(Http2Socket socket) {
			log.info("far end closed");
		}

		@Override
		public Http2SocketDataReader newIncomingPush(Http2Headers req, Http2Headers resp) {
			return this;
		}

		@Override
		public void failure(Exception e) {
			log.warn("exception", e);
		}

		@Override
		public void incomingData(DataWrapper data) {
			log.info("data");
		}

		@Override
		public void incomingTrailingHeaders(Http2Headers endHeaders) {
			log.info("done with data");
		}

		@Override
		public void serverCancelledRequest() {
			log.info("this request was cancelled by remote end");
		}
		
	}
	
	private static class ChunkedResponseListener implements Http2ResponseListener {

		@Override
		public void incomingResponse(Http2Headers resp, boolean isComplete) {
			log.info("incoming response="+resp);
		}

		@Override
		public void incomingData(DataWrapper data, boolean isComplete) {
			log.info("incoming data for response="+data);
		}

		@Override
		public void incomingEndHeaders(Http2Headers headers, boolean isComplete) {
			log.info("incoming end headers="+headers);
		}

		@Override
		public void serverCancelledRequest() {
			log.info("server cancelled request");
		}

		@Override
		public void incomingUnknownFrame(Http2UnknownFrame frame, boolean isComplete) {
			log.info("unknown frame="+frame);
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
