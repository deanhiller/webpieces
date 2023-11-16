package org.webpieces.http2client.api;

import java.net.InetSocketAddress;

import org.webpieces.util.HostWithPort;
import org.webpieces.util.futures.XFuture;

import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;

import com.webpieces.http2.api.streaming.RequestStreamHandle;

public interface Http2Socket {

    XFuture<Void> connect(HostWithPort addr);

    /**
     * @deprecated
     */
    @Deprecated
	XFuture<Void> connect(InetSocketAddress addr);
	
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
    XFuture<FullResponse> send(FullRequest request);

    /**
     * You should have a pretty good understanding the http/2 spec to use this method.  This method supports EVERY use-case
     * that http/2 has to offer (pretty much).
     * 
     * Http1.1 and Http\2 are both a bit complex resulting in a more complex api IF you want to support all use-cases
     * other than just the request/response use-case without blowing up your RAM including
     *
     *  1. send request headers only, then receive response headers and data chunks forever (twitter api does this)
     *  2. send request headers only, then receive response headers and data chunks until end chunk is received
     *     -With this, you can stream chunks through your system so it works for very very large file use-cases without
     *      storing the whole file in RAM as you can stream it through the system
     *  3. send request headers, then stream request chunks forever (this is sometimes a use-case)
     *  4. send request headers and send data chunks, then receive response headers
     *  5. send request headers and send data chunks, then receive response headers and data chunks
     *  
     */
    RequestStreamHandle openStream();
    
    XFuture<Void> close();

    /**
     * Future is complete when ping response is returned such that you can measure latency
     */
    XFuture<Void> sendPing();
    
}
