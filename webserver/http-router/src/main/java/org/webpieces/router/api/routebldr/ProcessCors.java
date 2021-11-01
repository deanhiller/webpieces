package org.webpieces.router.api.routebldr;


import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.RequestContext;

import java.util.List;

public interface ProcessCors {

    AccessResult isAccessAllowed(RequestContext ctx);

    /**
     * @param request Http2Request if the router is in webpieces else other platforms request
     * @return Http2Response going back
     */
    void processOptionsCors(Http2Request request, List<HttpMethod> methods, ResponseStreamHandle responseHandle);

}
