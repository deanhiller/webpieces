package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.Http2Client;
import org.webpieces.httpclient.api.Http2ClientFactory;
import org.webpieces.httpclient.api.Http2ResponseListener;
import org.webpieces.httpclient.api.Http2ServerListener;
import org.webpieces.httpclient.api.Http2Socket;
import org.webpieces.httpclient.api.Http2SocketDataReader;
import org.webpieces.httpclient.api.dto.Http2EndHeaders;
import org.webpieces.httpclient.api.dto.Http2Request;
import org.webpieces.httpclient.api.dto.Http2Response;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.http2parser.api.Http2Parser;
import com.webpieces.http2parser.api.Http2ParserFactory;

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
		
//		HttpRequestLine requestLine = new HttpRequestLine();
//		requestLine.setMethod(KnownHttpMethod.GET);
//		requestLine.setUri(new HttpUri("/"));
//		
//		HttpRequest req = new HttpRequest();
//		req.setRequestLine(requestLine);
//		req.addHeader(new Header(KnownHeaderName.HOST, host));
//		req.addHeader(new Header(KnownHeaderName.ACCEPT, "*/*"));
//		req.addHeader(new Header(KnownHeaderName.USER_AGENT, "webpieces/0.9"));
		
		Http2Request req = null;
		
		InetSocketAddress addr = new InetSocketAddress(host, port);
		Http2Socket socket = createHttpClient("testRunSocket", isHttp, addr);
		
		socket
			.connect(addr, null)
			.thenAccept(s -> s.sendRequest(req, new ChunkedResponseListener(), true))
			.exceptionally(e -> reportException(socket, e));
		
		Thread.sleep(100000);
	}

	public static Http2Socket createHttpClient(String id, boolean isHttp, InetSocketAddress addr) {
		BufferPool pool2 = new BufferCreationPool();
		Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("client", pool2, executor2);
		
		Http2Parser http2Parser = Http2ParserFactory.createParser(pool2);
		
		String host = addr.getHostName();
		int port = addr.getPort();
		ForTestSslClientEngineFactory ssl = new ForTestSslClientEngineFactory();
		SSLEngine engine = ssl.createSslEngine(host, port);
		
		Http2Client client = Http2ClientFactory.createHttpClient(mgr, http2Parser);
		
		Http2Socket socket;
		if(isHttp) {
			socket = client.createHttpSocket(id);
		} else {
			socket = client.createHttpsSocket(id, engine);
		}
		
		return socket;
	}

	private Void reportException(Http2Socket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}

	private static class ServerListenerImpl implements Http2ServerListener, Http2SocketDataReader {

		@Override
		public void farEndClosed(Http2Socket socket) {
			log.info("far end closed");
		}

		@Override
		public Http2SocketDataReader newIncomingPush(Http2Request req, Http2Response resp) {
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
		public void incomingTrailingHeaders(Http2EndHeaders endHeaders) {
			log.info("done with data");
		}

		@Override
		public void serverCancelledRequest() {
			log.info("this request was cancelled by remote end");
		}
		
	}
	
	private static class ChunkedResponseListener implements Http2ResponseListener {

		@Override
		public void incomingResponse(Http2Response resp) {
			log.info("incoming response="+resp);
		}

		@Override
		public void incomingData(DataWrapper data) {
			log.info("incoming data for response="+data);
		}

		@Override
		public void incomingEndHeaders(Http2EndHeaders headers) {
			log.info("incoming end headers="+headers);
		}

		@Override
		public void serverCancelledRequest() {
			log.info("server cancelled request");
		}
	}
}
