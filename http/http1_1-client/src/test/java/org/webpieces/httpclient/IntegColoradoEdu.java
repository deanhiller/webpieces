package org.webpieces.httpclient;

import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

public class IntegColoradoEdu {

	private static final Logger log = LoggerFactory.getLogger(IntegColoradoEdu.class);
	
	public static void main(String[] args) throws InterruptedException {
		boolean isHttp = false;
		
		String host = "www.colorado.edu";
		int port = 443;
		if(isHttp)
			port = 80;
		
		HttpRequest req = createRequest(host);

		log.info("starting socket");
		ChunkedResponseListener listener = new ChunkedResponseListener();
		
		HttpSocket socket = IntegGoogleHttps.createSocket(isHttp, host, port);
		
		socket
			.connect(new HostWithPort(host, port))
			.thenAccept(p -> socket.send(req, listener))
			.exceptionally(e -> reportException(socket, e));
		
		Thread.sleep(30000);
	}

	private static Void reportException(HttpSocket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}

	private static class ChunkedResponseListener implements HttpResponseListener {

		@Override
		public XFuture<HttpDataWriter> incomingResponse(HttpResponse resp, boolean isComplete) {
			log.info("received resp="+resp+" iscomplete="+isComplete);
			return XFuture.completedFuture(new Writer());
		}

		private class Writer implements HttpDataWriter {
			@Override
			public XFuture<Void> send(HttpData chunk) {
				log.info("received resp="+chunk+" last="+chunk.isEndOfData());
				return XFuture.completedFuture(null);
			}
		}
		
		@Override
		public void failure(Throwable e) {
			log.error("failed", e);
		}

	}
	
	private static HttpRequest createRequest(String host) {
//		GET / HTTP/1.1
//		Host: www.colorado.edu
//		User-Agent: curl/7.43.0
//		Accept: */*
		
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(KnownHttpMethod.GET);
		requestLine.setUri(new HttpUri("/"));
		
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);
		req.addHeader(new Header(KnownHeaderName.HOST, host));
		req.addHeader(new Header(KnownHeaderName.ACCEPT, "*/*"));
		req.addHeader(new Header(KnownHeaderName.USER_AGENT, "webpieces/0.9"));
		return req;
	}
}
