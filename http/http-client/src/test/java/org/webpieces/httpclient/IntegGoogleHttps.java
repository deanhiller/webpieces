package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.httpclient.api.HttpClientSocket;
import org.webpieces.httpcommon.api.Http2SettingsMap;
import org.webpieces.httpcommon.api.HttpSocket;
import org.webpieces.httpcommon.api.RequestSender;
import org.webpieces.httpcommon.api.ResponseId;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpcommon.api.ServerListener;
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
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2parser.api.dto.lib.Http2Header;

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
		
		HttpClientSocket socket = client.openHttpSocket("oneTimer", new OurCloseListener());
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
		HpackParser http2Parser = HpackParserFactory.createParser(pool2, true);
		
		HttpClient client;
		if(isHttp)
			client = HttpClientFactory.createHttpClient(mgr, httpParser, http2Parser);
		else {
			ForTestSslClientEngineFactory sslFactory = new ForTestSslClientEngineFactory();
			client = HttpClientFactory.createHttpsClient(mgr, httpParser, http2Parser, sslFactory, new Http2SettingsMap());
		}
		return client;
	}

	private void sendRequest(RequestSender requestListener, HttpRequest req) {
		requestListener.sendRequest(req, true, new OurListener());
	}

	private Void reportException(HttpClientSocket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}

	private static class OurListener implements ResponseListener {
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
		public void incomingTrailer(List<Http2Header> headers, ResponseId id, boolean isComplete) {
			log.info("received trailer" + headers +" id=" + id + " last="+ isComplete);
		}

		@Override
		public void failure(Throwable e) {
			log.error("failed", e);
		}
	}
	
	private class OurCloseListener implements ServerListener {
		@Override
		public void farEndClosed(HttpSocket socket) {
			log.info(socket+" far end closed");
		}

	}
}
