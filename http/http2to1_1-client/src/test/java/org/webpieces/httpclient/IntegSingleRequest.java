package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLEngine;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.data.api.TwoPools;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2ClientFactory;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.ChannelManager;
import org.webpieces.nio.api.ChannelManagerFactory;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.hpack.api.HpackParser;
import com.webpieces.hpack.api.HpackParserFactory;
import com.webpieces.http2.api.dto.highlevel.Http2Push;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.PushPromiseListener;
import com.webpieces.http2.api.streaming.PushStreamHandle;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;
import com.webpieces.http2engine.api.client.InjectionConfig;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

public class IntegSingleRequest {

	private static final Logger log = LoggerFactory.getLogger(IntegSingleRequest.class);
	
	public static void main(String[] args) throws InterruptedException {
		new IntegSingleRequest().start();
	}
	
	public void start() throws InterruptedException {
		log.info("starting test to download / page from google");

		boolean isHttp = true;
		
		String host = "www.google.com"; 
		//String host = "localhost"; //jetty
		//String host = "api.push.apple.com";
		//String host = "gcm-http.googleapis.com";
		//String host = "nghttp2.org";
		int port = 443;
		if(isHttp)
			port = 80;
		
		if("localhost".equals(host)) {
			port = 8443;
			if(isHttp)
				port = 8080;
		}
		
		List<Http2Header> req = createRequest(host, isHttp);
		Http2Request request = new Http2Request(req);
        request.setEndOfStream(true);
        
		InetSocketAddress addr = new InetSocketAddress(host, port);
		Http2Socket socket = createHttpClient("testRunSocket", isHttp, addr);
		
		socket
			.connect(addr)
			.thenAccept(v -> socket.openStream().process(request, new ChunkedResponseListener()))
			.exceptionally(e -> reportException(socket, e));
		
		Thread.sleep(10000000);
	}

	public static Http2Socket createHttpClient(String id, boolean isHttp, InetSocketAddress addr) {
		BufferPool pool2 = new TwoPools("pl", new SimpleMeterRegistry());
		HpackParser hpackParser = HpackParserFactory.createParser(pool2, false);

		Executor executor2 = Executors.newFixedThreadPool(10, new NamedThreadFactory("clientThread"));
		ChannelManagerFactory factory = ChannelManagerFactory.createFactory(Metrics.globalRegistry);
		ChannelManager mgr = factory.createMultiThreadedChanMgr("client", pool2, new BackpressureConfig(), executor2);
		
		InjectionConfig injConfig = new InjectionConfig(hpackParser);
		
		String host = addr.getHostName();
		int port = addr.getPort();
		ForTestSslClientEngineFactory ssl = new ForTestSslClientEngineFactory();
		SSLEngine engine = ssl.createSslEngine(host, port);
		
		Http2Client client = Http2ClientFactory.createHttpClient("myClient", mgr, injConfig);
		
		Http2Socket socket;
		if(isHttp) {
			socket = client.createHttpSocket(new Http2CloseListener());
		} else {
			socket = client.createHttpsSocket(engine, new Http2CloseListener());
		}
		
		return socket;
	}

	private Void reportException(Http2Socket socket, Throwable e) {
		log.error("exception on socket="+socket, e);
		return null;
	}
	
	private static class ChunkedResponseListener implements ResponseStreamHandle, PushPromiseListener, PushStreamHandle {
		@Override
		public XFuture<StreamWriter> process(Http2Response response) {
			log.info("incoming part of response="+response);
			return XFuture.completedFuture(null);
		}

		@Override
		public PushStreamHandle openPushStream() {
			return this;
		}
		
		@Override
		public XFuture<StreamWriter> processPushResponse(Http2Response response) {
			log.info("incoming push promise. response="+response);
			return XFuture.completedFuture(null);
		}

		@Override
		public XFuture<Void> cancel(CancelReason frame) {
			return XFuture.completedFuture(null);
		}

		@Override
		public XFuture<Void> cancelPush(CancelReason payload) {
			return XFuture.completedFuture(null);
		}
		
		@Override
		public XFuture<PushPromiseListener> process(Http2Push headers) {
			return XFuture.completedFuture(this);
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
        headers.add(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
        headers.add(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
        headers.add(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/1.15.0"));

        return headers;
    }
}
