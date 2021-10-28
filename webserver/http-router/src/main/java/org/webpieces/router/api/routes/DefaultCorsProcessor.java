package org.webpieces.router.api.routes;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.Http2Method;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.impl.routebldr.ProcessCors;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DefaultCorsProcessor implements ProcessCors {

    private final Set<String> allowedDomains;
    private final Set<String> allowedHeaders;

    public DefaultCorsProcessor(Set<String> allowedDomains, Set<String> allowedHeaders) {
        this.allowedDomains = allowedDomains;
        this.allowedHeaders = allowedHeaders;
    }

    @Override
    public Http2Response processOptionsCors(Http2Request request, List<HttpMethod> methods, ResponseStreamHandle responseStream) {
//        Http2Header header = request.getHeaderLookupStruct().getHeader(Http2HeaderName.ORIGIN);
//        if(header == null)
//            throw new IllegalStateException("Should only use this for CORS which requires an Origin header");
//
//        Http2Response response = new Http2Response();


        return null;
    }
}
