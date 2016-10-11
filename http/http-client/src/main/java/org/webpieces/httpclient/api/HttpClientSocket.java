package org.webpieces.httpclient.api;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpcommon.api.HttpSocket;
import org.webpieces.httpcommon.api.RequestSender;

public interface HttpClientSocket extends HttpSocket {


    /**
     *
     * Connects to an HTTP server at a given address, and returns a RequestSender
     * that one can use to send requests to that HTTP server.
     *
     * @param addr
     * @return
     */
    CompletableFuture<RequestSender> connect(InetSocketAddress addr);

    RequestSender getRequestSender();
	
}
