package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;
import org.webpieces.httpclient.api.*;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

public class IntegGoogleHttps {

	private static final Logger log = LoggerFactory.getLogger(IntegGoogleHttps.class);
	
	public static void main(String[] args) throws InterruptedException {
		new IntegGoogleHttps().start();
	}
	
	public void start() throws InterruptedException {
		log.info("starting test to download / page from google");

		boolean isHttp = false;
		
		String host = "www.google.com";
		int port = 443;
		if(isHttp)
			port = 80;
		
		HttpRequestLine requestLine = new HttpRequestLine();
		requestLine.setMethod(KnownHttpMethod.GET);
		requestLine.setUri(new HttpUri("/"));
		
		HttpRequest req = new HttpRequest();
		req.setRequestLine(requestLine);
		req.addHeader(new Header(KnownHeaderName.HOST, host));
		req.addHeader(new Header(KnownHeaderName.ACCEPT, "*/*"));
		req.addHeader(new Header(KnownHeaderName.USER_AGENT, "webpieces/0.9"));
		
		HttpClient client = createHttpClient(isHttp);
		
		HttpSocket socket = client.openHttpSocket("oneTimer", new OurCloseListener());
		socket
			.connect(new InetSocketAddress(host, port))
			.thenAccept(requestListener -> sendRequest(requestListener, req))
			.exceptionally(e -> reportException(socket, e));
		
		Thread.sleep(100000);
	}

	public static HttpClient createHttpClient(boolean isHttp) {
		BufferPool pool2 = new BufferCreationPool();
		Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("client", pool2, executor2);
		
		HttpParser httpParser = HttpParserFactory.createParser(pool2);
		Http2Parser http2Parser = Http2ParserFactory.createParser(pool2);
		
		HttpClient client;
		if(isHttp)
			client = HttpClientFactory.createHttpClient(mgr, httpParser, http2Parser);
		else {
			ForTestSslClientEngineFactory sslFactory = new ForTestSslClientEngineFactory();
			client = HttpClientFactory.createHttpsClient(mgr, httpParser, http2Parser, sslFactory);
		}
		return client;
	}

	private void sendRequest(RequestListener requestListener, HttpRequest req) {
		requestListener.incomingRequest(req, true, new OurListener());
	}

	private Void reportException(HttpSocket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}

	private static class OurListener implements ResponseListener {
		@Override
		public void incomingResponse(HttpResponse resp, boolean isComplete) {
			log.info("resp="+resp+" complete="+isComplete);
		}

		@Override
		public void incomingResponse(HttpResponse resp, HttpRequest req, boolean isComplete) {
			log.info("req="+req);
			incomingResponse(resp, isComplete);
		}

		@Override
		public CompletableFuture<Integer> incomingData(DataWrapper data, HttpRequest request, boolean isLastData) {
			return incomingData(data, isLastData);
		}

		@Override
		public CompletableFuture<Integer> incomingData(DataWrapper wrapper, boolean isLastData) {
			String result = wrapper.createStringFrom(0, wrapper.getReadableSize(), HttpParserFactory.iso8859_1);
			log.info("result=(lastData="+ isLastData +"\n"+result+"/////");
			return CompletableFuture.completedFuture(wrapper.getReadableSize());
		}

		@Override
		public void failure(Throwable e) {
			log.error("exception", e);
		}
	}
	
	private class OurCloseListener implements CloseListener {
		@Override
		public void farEndClosed(HttpSocket socket) {
			log.info(socket+" far end closed");
		}
		
	}
}
