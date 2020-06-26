package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.TwoPools;
import org.webpieces.httpclient11.api.HttpClient;
import org.webpieces.httpclient11.api.HttpClientFactory;
import org.webpieces.httpclient11.api.HttpDataWriter;
import org.webpieces.httpclient11.api.HttpResponseListener;
import org.webpieces.httpclient11.api.HttpSocket;
import org.webpieces.httpparser.api.HttpParser;
import org.webpieces.httpparser.api.HttpParserFactory;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpData;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

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
		
		HttpSocket socket = createSocket(isHttp, host, port);
		socket
			.connect(new InetSocketAddress(host, port))
			.thenAccept(p -> sendRequest(socket, req))
			.exceptionally(e -> reportException(socket, e));
		
		Thread.sleep(100000);
	}

	public static HttpSocket createSocket(boolean isHttp, String host, int port) {
		HttpClient client = createHttpClient();
		HttpSocket socket;
		if(isHttp)
			socket = client.createHttpSocket(new SocketListener());
		else {
			ForTestSslClientEngineFactory sslFactory = new ForTestSslClientEngineFactory();
			socket = client.createHttpsSocket(sslFactory.createSslEngine(host, port), new SocketListener());
		}
		return socket;
	}
	
	public static HttpClient createHttpClient() {
		BufferPool pool2 = new TwoPools("pl", new SimpleMeterRegistry());
		Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(Metrics.globalRegistry);
		ChannelManager mgr = factory.createMultiThreadedChanMgr("client", pool2, new BackpressureConfig(), executor2);
		
		HttpParser parser = HttpParserFactory.createParser("a", new SimpleMeterRegistry(), pool2);
		
		HttpClient client = HttpClientFactory.createHttpClient("myClient", mgr, parser);
		return client;
	}

	private void sendRequest(HttpSocket socket, HttpRequest req) {
		socket.send(req, new OurListener());
	}

	private Void reportException(HttpSocket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}

	private static class OurListener implements HttpResponseListener {
		@Override
		public CompletableFuture<HttpDataWriter> incomingResponse(HttpResponse resp, boolean isComplete) {
			log.info("resp="+resp+" complete="+isComplete);
			return CompletableFuture.completedFuture(new Writer());
		}

		private class Writer implements HttpDataWriter {
			@Override
			public CompletableFuture<Void> send(HttpData chunk) {
				DataWrapper wrapper = chunk.getBody();
				String result = wrapper.createStringFrom(0, wrapper.getReadableSize(), HttpParserFactory.ISO8859_1);
				log.info("result=(lastChunk="+chunk.isEndOfData()+")\n"+result+"/////");
				return CompletableFuture.completedFuture(null);
			}
		}


		@Override
		public void failure(Throwable e) {
			log.error("exception", e);
		}
		
	}
	
}
