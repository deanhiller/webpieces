package org.webpieces.router.impl.routers;

import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.streaming.StreamWriter;
import org.webpieces.router.impl.proxyout.ProxyStreamHandle;

import java.util.concurrent.CompletableFuture;

public class CorsProxyStreamHandle extends ProxyStreamHandle {
    private ProxyStreamHandle handler;
    private AbstractRouter router;

    public CorsProxyStreamHandle(ProxyStreamHandle handler) {
        super(null, null, null, null);
        this.handler = handler;

        throw new IllegalStateException("excpeiotn");
    }

    public void setRouter(AbstractRouter router) {
        this.router = router;
    }

    @Override
    public CompletableFuture<StreamWriter> process(Http2Response response) {
        //modify response here...
        return handle.process(response);
    }
}
