package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;

import org.webpieces.data.api.BufferCreationPool;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2ServerListener;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.hpack.api.dto.Http2Headers;
import com.webpieces.http2engine.api.client.Http2ClientEngineFactory;
import com.webpieces.http2engine.api.client.Http2ResponseListener;
import com.webpieces.http2engine.api.client.PushPromiseListener;
import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;
import com.webpieces.http2parser.api.dto.lib.PartialStream;

public class IntegSingleRequest {

	private static final Logger log = LoggerFactory.getLogger(IntegSingleRequest.class);
	
	public static void main(String[] args) throws InterruptedException {
		new IntegSingleRequest().start();
	}
	
	public void start() throws InterruptedException {
		log.info("starting test to download / page from google");

		boolean isHttp = false;
		
		//String host = "www.google.com"; 
		//String host = "localhost"; //jetty
		String host = "nghttp2.org";
		int port = 443;
		if(isHttp)
			port = 80;
		
		if("localhost".equals(host)) {
			port = 8443;
			if(isHttp)
				port = 8080;
		}
		
		List<Http2Header> req = createRequest(host, isHttp);
    	Http2Headers request = new Http2Headers(req);
        request.setEndOfStream(true);
        
		InetSocketAddress addr = new InetSocketAddress(host, port);
		Http2Socket socket = createHttpClient("testRunSocket", isHttp, addr);
		
		socket
			.connect(addr, new ServerListenerImpl())
			.thenAccept(s -> s.sendRequest(request, new ChunkedResponseListener()))
			.exceptionally(e -> reportException(socket, e));
		
		Thread.sleep(10000000);
	}

	public static Http2Socket createHttpClient(String id, boolean isHttp, InetSocketAddress addr) {
		BufferPool pool2 = new BufferCreationPool();
		Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory();
		ChannelManager mgr = factory.createMultiThreadedChanMgr("client", pool2, executor2);
		
		HpackParser hpackParser = HpackParserFactory.createParser(pool2, false);
		Http2ClientEngineFactory http2HighLevelFactory = new Http2ClientEngineFactory();
		
		String host = addr.getHostName();
		int port = addr.getPort();
		ForTestSslClientEngineFactory ssl = new ForTestSslClientEngineFactory();
		SSLEngine engine = ssl.createSslEngine(host, port);
		
		Http2Client client = Http2ClientFactory.createHttpClient(mgr, hpackParser, http2HighLevelFactory);
		
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
		public CompletableFuture<Void> incomingPartialResponse(PartialStream response) {
			log.info("incoming part of response="+response);
			return CompletableFuture.completedFuture(null);
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
		public CompletableFuture<Void> incomingPushPromise(PartialStream response) {
			log.info("incoming push promise. response="+response);
			return CompletableFuture.completedFuture(null);
		}
	}
	
    private static List<Http2Header> createRequest(String host, boolean isHttp) {
    	String scheme;
    	if(isHttp)
    		scheme = "http";
    	else
    		scheme = "https";
    	
    	List<Http2Header> headers = new ArrayList<>();
    	
        headers.add(new Http2Header(Http2HeaderName.METHOD, "GET"));
        headers.add(new Http2Header(Http2HeaderName.AUTHORITY, host));
        headers.add(new Http2Header(Http2HeaderName.PATH, "/"));
        headers.add(new Http2Header(Http2HeaderName.SCHEME, scheme));
        headers.add(new Http2Header(Http2HeaderName.HOST, host));
        headers.add(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
        headers.add(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
        headers.add(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/1.15.0"));

        return headers;
    }
}
