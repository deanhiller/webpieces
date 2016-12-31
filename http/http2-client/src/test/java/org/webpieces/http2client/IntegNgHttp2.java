package org.webpieces.http2client;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.Http2ResponseListener;
import org.webpieces.http2client.api.Http2ServerListener;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.PushPromiseListener;
import org.webpieces.http2client.api.dto.Http2Headers;
import org.webpieces.http2client.api.dto.PartialResponse;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.http2parser.api.dto.GoAwayFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Frame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class IntegNgHttp2 {

    private static final Logger log = LoggerFactory.getLogger(IntegNgHttp2.class);
    private static final DataWrapperGenerator dataGen = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    static private CompletableFuture<Void> sendManyTimes(Http2Socket socket, int n, Http2Headers req, Http2ResponseListener l) {
        if(n > 0) {
        	log.info("send request");
            return socket.sendRequest(req, l, true)
                    .thenCompose(response -> sendManyTimes(socket, n-1, req, l));
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    public static void main(String[] args) throws InterruptedException {
        boolean isHttp = true;

        String host = "nghttp2.org";
        int port = 443;
        if(isHttp)
            port = 80;

        Http2Headers req = createRequest(host, isHttp);

        log.info("starting socket");
        ChunkedResponseListener listener = new ChunkedResponseListener();

        InetSocketAddress addr = new InetSocketAddress(host, port);
        Http2Socket socket = IntegGoogleHttps.createHttpClient("clientSocket", isHttp, addr);

        socket
                .connect(addr, new ServerListenerImpl())
                .thenCompose(theSocket -> {
                	log.info("sending REQUEST");
                	return sendManyTimes(theSocket, 1, req, listener);
                	})
                .exceptionally(e -> {
                    reportException(socket, e);
                    return null;
                });

        Thread.sleep(100000);

        sendManyTimes(socket, 1, req, listener).exceptionally(e -> {
            reportException(socket, e);
            return null;
        });
        Thread.sleep(10000);
    }

    private static Void reportException(Http2Socket socket, Throwable e) {
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
			log.info("incoming push promise="+response);
		}
		
	}

    private static Http2Headers createRequest(String host, boolean isHttp) {
    	String scheme;
    	if(isHttp)
    		scheme = "http";
    	else
    		scheme = "https";
    	
    	Http2Headers req = new Http2Headers();
        req.addHeader(new Http2Header(Http2HeaderName.METHOD, "GET"));
        req.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, host));
        req.addHeader(new Http2Header(Http2HeaderName.PATH, "/"));
        req.addHeader(new Http2Header(Http2HeaderName.SCHEME, scheme));
        req.addHeader(new Http2Header("host", host));
        req.addHeader(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
        req.addHeader(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
        req.addHeader(new Http2Header(Http2HeaderName.USER_AGENT, "nghttp2/1.15.0"));
        
        return req;
    }
}
