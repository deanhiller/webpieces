package org.webpieces.httpclient;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpclient.api.*;
import org.webpieces.httpcommon.api.RequestId;
import org.webpieces.httpcommon.api.RequestSender;
import org.webpieces.httpcommon.api.ResponseListener;
import org.webpieces.httpparser.api.common.Header;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpRequestLine;
import org.webpieces.httpparser.api.dto.HttpResponse;
import org.webpieces.httpparser.api.dto.HttpUri;
import org.webpieces.httpparser.api.dto.KnownHttpMethod;

public class IntegNgHttp2 {

    private static final Logger log = LoggerFactory.getLogger(IntegNgHttp2.class);

    static private CompletableFuture<HttpRequest> sendManyTimes(RequestSender requestListener, int n, HttpRequest req, ResponseListener l) {
        if(n > 0) {
            return requestListener.sendRequest(req, true, l)
                    .thenCompose(request -> sendManyTimes(requestListener, n-1, req, l));
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

        HttpRequest req = createRequest(host);

        log.info("starting socket");
        ChunkedResponseListener listener = new ChunkedResponseListener();

        HttpClient client = IntegGoogleHttps.createHttpClient(isHttp);

        HttpClientSocket socket = client.openHttpSocket("oneTimer");
        socket
                .connect(new InetSocketAddress(host, port))
                .thenCompose(requestListener -> sendManyTimes(requestListener, 10, req, listener))
                .exceptionally(e -> {
                    reportException(socket, e);
                    return req;
                });

        Thread.sleep(10000);

        sendManyTimes(socket.getRequestSender(), 10, req, listener).exceptionally(e -> {
            reportException(socket, e);
            return req;
        });
        Thread.sleep(10000);
    }

    private static Void reportException(HttpClientSocket socket, Throwable e) {
        log.error("exception on socket="+socket, e);
        return null;
    }

    private static class ChunkedResponseListener implements ResponseListener {

        @Override
        public void incomingResponse(HttpResponse resp, HttpRequest req, RequestId id, boolean isComplete) {
            log.info("received req="+req+"resp="+resp+" id=" + id +" iscomplete="+isComplete);
        }

        @Override
        public CompletableFuture<Void> incomingData(DataWrapper data, RequestId id, boolean isLastData) {
            log.info("received resp="+ data +" id=" + id + " last="+ isLastData);
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void failure(Throwable e) {
            log.error("failed", e);
        }

    }


    private static HttpRequest createRequest(String host) {
        HttpRequestLine requestLine = new HttpRequestLine();
        requestLine.setMethod(KnownHttpMethod.GET);
        requestLine.setUri(new HttpUri("/"));

        HttpRequest req = new HttpRequest();
        req.setRequestLine(requestLine);
        req.addHeader(new Header(KnownHeaderName.HOST, host));
        req.addHeader(new Header(KnownHeaderName.ACCEPT, "*/*"));
        req.addHeader(new Header(KnownHeaderName.ACCEPT_ENCODING, "gzip, deflate"));
        req.addHeader(new Header(KnownHeaderName.USER_AGENT, "nghttp2/1.15.0"));
        return req;
    }
}
