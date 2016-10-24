package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.webpieces.http2parser.api.dto.HasHeaderFragment;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

public class IntegColoradoEdu {

	private static final Logger log = LoggerFactory.getLogger(IntegColoradoEdu.class);
	
	public static void main(String[] args) throws InterruptedException {
		boolean isHttp = true;
		
		String host = "www.colorado.edu";
		int port = 443;
		if(isHttp)
			port = 80;
		
		HttpRequest req = createRequest(host);

		log.info("starting socket");
		ChunkedResponseListener listener = new ChunkedResponseListener();
		
		HttpClient client = IntegGoogleHttps.createHttpClient(isHttp);
		
		HttpClientSocket socket = client.openHttpSocket("oneTimer");
		socket
			.connect(new InetSocketAddress(host, port))
			.thenAccept(requestListener -> requestListener.sendRequest(req, true, listener))
			.exceptionally(e -> reportException(socket, e));

		Thread.sleep(100000);
	}

	private static Void reportException(HttpClientSocket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}

	private static class ChunkedResponseListener implements ResponseListener {

		@Override
		public void incomingResponse(HttpResponse resp, HttpRequest req, ResponseId id, boolean isComplete) {
			log.info("received req="+req+"resp="+resp+" id=" + id +" iscomplete="+isComplete);
		}

		@Override
		public CompletableFuture<Void> incomingData(DataWrapper data, ResponseId id, boolean isLastData) {
			log.info("received resp="+ data +" id=" + id + " last="+ isLastData);
			return CompletableFuture.completedFuture(null);
		}

		@Override
		public void incomingTrailer(List<HasHeaderFragment.Header> headers, ResponseId id, boolean isComplete) {
			log.info("received trailer" + headers +" id=" + id + " last="+ isComplete);
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
