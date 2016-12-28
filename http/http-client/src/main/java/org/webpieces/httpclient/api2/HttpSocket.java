package org.webpieces.httpclient.api2;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.HttpRequest;
import org.webpieces.httpparser.api.dto.HttpResponse;

public interface HttpSocket {
	
	CompletableFuture<HttpSocket> connect(InetSocketAddress addr, HttpServerListener listener);
	
    /**
     * This can be used ONLY if 'you' know that the far end does NOT send a chunked download
     * or in the case of http2, too large of a data download blowing your RAM.
     * The reason is in a chunked download, we don't want to blow up your RAM.  Some apis like
     * twitters streaming api would never ever be done and have a full response.  Others
     * are just a very very large download you don't want existing in RAM anyways and you would
     * rather receive a chunk, and write it to file or db, receive another and do the same to
     * keep your RAM low.
     *
     * @param request
     */
    //TODO: Implement timeout for clients so that requests will timeout
    CompletableFuture<HttpResponse> send(HttpRequest request);

    /**
     * Http1.1 and Http\2 are both a bit complex resulting in a more complex api BUT this api does support every
     * scenario including
     * 
     *  1. send request headers(in HttpRequest), then stream request chunks forever (this is sometimes a use-case)
     *  2. send request headers only, then receive response headers and data chunks forever (twitter api does this)
     *  3. send request headers only, then receive response headers and data chunks until end chunk is received
     *  4. send request headers and send data chunks, then receive response headers
     *  5. send request headers and send data chunks, then receive response headers and data chunks
     *  
     *  @param isComplete true if you are only sending request headers with content-length = 0
     */
    CompletableFuture<HttpSocketDataWriter> sendRequest(HttpRequest request, HttpResponseListener listener, boolean isComplete);
    
    CompletableFuture<Void> close();
    
}
