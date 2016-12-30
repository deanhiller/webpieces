package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.Http2ResponseListener;
import org.webpieces.httpclient.api.Http2ServerListener;
import org.webpieces.httpclient.api.Http2Socket;
import org.webpieces.httpclient.api.Http2SocketDataReader;
import org.webpieces.httpclient.api.dto.Http2Headers;

import com.webpieces.http2parser.api.dto.Http2UnknownFrame;
import com.webpieces.http2parser.api.dto.lib.Http2Header;
import com.webpieces.http2parser.api.dto.lib.Http2HeaderName;

public class IntegNgHttp2 {

    private static final Logger log = LoggerFactory.getLogger(IntegNgHttp2.class);

    static private CompletableFuture<Http2Headers> sendManyTimes(Http2Socket socket, int n, Http2Headers req, Http2ResponseListener l) {
        if(n > 0) {
            return socket.sendRequest(req, l, true)
                    .thenCompose(dataWriter -> sendManyTimes(socket, n-1, req, l));
        } else {
            return CompletableFuture.completedFuture(req);
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
                .thenCompose(theSocket -> sendManyTimes(theSocket, 1, req, listener))
                .exceptionally(e -> {
                    reportException(socket, e);
                    return req;
                });

        Thread.sleep(100000);

        sendManyTimes(socket, 1, req, listener).exceptionally(e -> {
            reportException(socket, e);
            return req;
        });
        Thread.sleep(10000);
    }

    private static Void reportException(Http2Socket socket, Throwable e) {
        log.error("exception on socket="+socket, e);
        return null;
    }


	private static class ServerListenerImpl implements Http2ServerListener, Http2SocketDataReader {

		@Override
		public void farEndClosed(Http2Socket socket) {
			log.info("far end closed");
		}

		@Override
		public Http2SocketDataReader newIncomingPush(Http2Headers req, Http2Headers resp) {
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
		public void incomingTrailingHeaders(Http2Headers endHeaders) {
			log.info("done with data");
		}

		@Override
		public void serverCancelledRequest() {
			log.info("this request was cancelled by remote end");
		}
		
	}
	
	private static class ChunkedResponseListener implements Http2ResponseListener {

		@Override
		public void incomingResponse(Http2Headers resp, boolean isComplete) {
			log.info("incoming response="+resp);
		}

		@Override
		public void incomingData(DataWrapper data, boolean isComplete) {
			log.info("incoming data for response="+data);
		}

		@Override
		public void incomingEndHeaders(Http2Headers headers, boolean isComplete) {
			log.info("incoming end headers="+headers);
		}

		@Override
		public void serverCancelledRequest() {
			log.info("server cancelled request");
		}
		
		@Override
		public void incomingUnknownFrame(Http2UnknownFrame frame, boolean isComplete) {
			log.info("unknown frame="+frame);
		}
	}

    private static Http2Headers createRequest(String host, boolean isHttps) {
    	String scheme;
    	if(isHttps)
    		scheme = "https";
    	else
    		scheme = "http";
    	
    	Http2Headers req = new Http2Headers();
        req.addHeader(new Http2Header(Http2HeaderName.METHOD, "GET"));
        req.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, host));
        req.addHeader(new Http2Header(Http2HeaderName.PATH, "/"));
        req.addHeader(new Http2Header(Http2HeaderName.SCHEME, scheme));
        req.addHeader(new Http2Header(Http2HeaderName.ACCEPT, "*/*"));
        req.addHeader(new Http2Header(Http2HeaderName.ACCEPT_ENCODING, "gzip, deflate"));
        req.addHeader(new Http2Header(Http2HeaderName.USER_AGENT, "webpieces/xx"));
        return req;
    }
}
