package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2ResponseListener;
import org.webpieces.http2client.api.Http2ServerListener;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.PushPromiseListener;
import org.webpieces.http2client.api.dto.Http2Headers;
import org.webpieces.http2client.api.dto.PartialResponse;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.http2engine.api.Http2EngineFactory;
import com.webpieces.http2parser.api.Http2Parser2;
import com.webpieces.http2parser.api.Http2ParserFactory;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.UnknownFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;

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
		
		Http2Headers req = null;
		
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
		
		Http2Parser2 http2Parser = Http2ParserFactory.createParser2(pool2);
		Http2EngineFactory http2HighLevelFactory = new Http2EngineFactory();
		
		String host = addr.getHostName();
		int port = addr.getPort();
		ForTestSslClientEngineFactory ssl = new ForTestSslClientEngineFactory();
		SSLEngine engine = ssl.createSslEngine(host, port);
		
		Http2Client client = Http2ClientFactory.createHttpClient(mgr, http2Parser, http2HighLevelFactory);
		
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

	private static class ServerListenerImpl implements Http2ServerListener {

		@Override
		public void farEndClosed(Http2Socket socket) {
			log.info("far end closed");
		}

		@Override
		public void failure(Exception e) {
			log.warn("exception", e);
		}

		@Override
		public void incomingControlFrame(Http2Frame lowLevelFrame) {
			if(lowLevelFrame instanceof GoAwayFrame) {
				GoAwayFrame goAway = (GoAwayFrame) lowLevelFrame;
				DataWrapper debugData = goAway.getDebugData();
				String debug = debugData.createStringFrom(0, debugData.getReadableSize(), StandardCharsets.UTF_8);
				log.info("go away received.  debug="+debug);
			} else 
				throw new UnsupportedOperationException("not done yet.  frame="+lowLevelFrame);
		}
	}
	
	private static class ChunkedResponseListener implements Http2ResponseListener, PushPromiseListener {
		@Override
		public void incomingPartialResponse(PartialResponse response) {
			log.info("incoming part of response="+response);
		}

		@Override
		public PushPromiseListener newIncomingPush(int streamId) {
			return this;
		}
		@Override
		public void serverCancelledRequest() {
			log.info("server cancelled request");
		}
		@Override
		public void incomingPushPromise(PartialResponse response) {
			log.info("incoming push promise");
		}
	}
}
