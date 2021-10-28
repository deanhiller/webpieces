package org.webpieces.router.impl.routebldr;


import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import org.webpieces.ctx.api.HttpMethod;

import java.util.List;

public interface ProcessCors {

    /**
     * @param request Http2Request if the router is in webpieces else other platforms request
     * @return Http2Response going back
     */
    Http2Response processOptionsCors(Http2Request request, List<HttpMethod> methods, ResponseStreamHandle responseHandle);

}
