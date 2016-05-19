package com.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpclient.api.CloseListener;
import org.webpieces.httpclient.api.HttpClient;
import org.webpieces.httpclient.api.HttpClientFactory;
import org.webpieces.httpclient.api.HttpSocket;
import org.webpieces.httpclient.api.ResponseListener;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.data.api.BufferCreationPool;
import com.webpieces.data.api.BufferPool;
import com.webpieces.data.api.DataWrapper;
import com.webpieces.httpparser.api.HttpParser;
import com.webpieces.httpparser.api.HttpParserFactory;
import com.webpieces.httpparser.api.common.Header;
import com.webpieces.httpparser.api.common.KnownHeaderName;
import com.webpieces.httpparser.api.dto.HttpChunk;
import com.webpieces.httpparser.api.dto.HttpRequest;
import com.webpieces.httpparser.api.dto.HttpRequestLine;
import com.webpieces.httpparser.api.dto.HttpResponse;
import com.webpieces.httpparser.api.dto.HttpUri;
import com.webpieces.httpparser.api.dto.KnownHttpMethod;

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
			.thenAccept(p -> sendRequest(socket, req))
			.exceptionally(e -> reportException(socket, e));
		
		Thread.sleep(100000);
	}

	public static HttpClient createHttpClient(boolean isHttp) {
		BufferPool pool2 = new BufferCreationPool();
		Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("client", pool2, executor2);
		
		HttpParser parser = HttpParserFactory.createParser(pool2);
		
		HttpClient client;
		if(isHttp)
			client = HttpClientFactory.createHttpClient(mgr, parser);
		else {
			ForTestSslClientEngineFactory sslFactory = new ForTestSslClientEngineFactory();
			client = HttpClientFactory.createHttpsClient(mgr, parser, sslFactory);
		}
		return client;
	}

	private void sendRequest(HttpSocket socket, HttpRequest req) {
		socket.send(req, new OurListener());
	}

	private Void reportException(HttpSocket socket, Throwable e) {
		log.warn("exception on socket="+socket, e);
		return null;
	}

	private static class OurListener implements ResponseListener {
		@Override
		public void incomingResponse(HttpResponse resp, boolean isComplete) {
			log.info("resp="+resp+" complete="+isComplete);
		}

		@Override
		public void incomingChunk(HttpChunk chunk, boolean isLastChunk) {
			DataWrapper wrapper = chunk.getBody();
			String result = wrapper.createStringFrom(0, wrapper.getReadableSize(), HttpParserFactory.iso8859_1);
			log.info("result=(lastChunk="+isLastChunk+"\n"+result+"/////");
		}

		@Override
		public void failure(Throwable e) {
			log.warn("exception", e);
		}
	}
	
	private class OurCloseListener implements CloseListener {
		@Override
		public void farEndClosed(HttpSocket socket) {
			log.info(socket+" far end closed");
		}
		
	}
}
