package org.webpieces.router.api.routes;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.Http2Method;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.http.exception.UnauthorizedException;
import org.webpieces.router.impl.routebldr.ProcessCors;
import org.webpieces.util.exceptions.SneakyThrow;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class DefaultCorsProcessor implements ProcessCors {

    private final Set<String> allowedDomains;
    private final Set<String> allowedHeaders;
    private Set<String> exposeTheseResponseHeadersToBrowser;
    private boolean isAllowCredsCookies;
    private int maxAgeSeconds;

    public DefaultCorsProcessor(
            Set<String> allowedDomains,
            Set<String> allowedRequestHeaders,
            Set<String> exposeTheseResponseHeadersToBrowser,
            boolean isAllowCredsCookies,
            int maxAgeSeconds

    ) {
        this.maxAgeSeconds = maxAgeSeconds;
        if(allowedDomains.size() <= 0)
            throw new IllegalArgumentException("Must contain a list of domains or one domain with value * for all domains");
        else if(isAllowCredsCookies && allowedDomains.contains("*"))
            throw new IllegalArgumentException("Having isAllowCreds and all domains is strictly forbidden by the CORS spec with good reason");

        this.allowedDomains = allowedDomains;
        this.allowedHeaders = allowedRequestHeaders;
        this.isAllowCredsCookies = isAllowCredsCookies;
        this.exposeTheseResponseHeadersToBrowser = exposeTheseResponseHeadersToBrowser;
    }

    @Override
    public void processOptionsCors(Http2Request request, List<HttpMethod> methods, ResponseStreamHandle responseStream) {
        Http2Header header = request.getHeaderLookupStruct().getHeader(Http2HeaderName.ORIGIN);
        if(header == null)
            throw new IllegalStateException("Should only use this for CORS which requires an Origin header");
        else if(!allowedDomains.contains("*") && !allowedDomains.contains(header.getValue())) {
            send403Response(responseStream, request);
            return;
        }

        Http2Response response = new Http2Response();
        response.addHeader(new Http2Header(Http2HeaderName.STATUS, "204"));
        response.addHeader(new Http2Header(Http2HeaderName.ACCESS_CONTROL_ALLOW_ORIGIN, header.getValue()));
        if(allowedDomains.contains("*")) {
            //since all domains, we must tell caches that Origin header in response will vary
            //since it responds with the domain that requested it
            response.addHeader(new Http2Header(Http2HeaderName.VARY, "Origin"));
        }

        if(isAllowCredsCookies) {
            response.addHeader(new Http2Header(Http2HeaderName.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true"));
        }

        String allowedMethodsStr = methods.stream().map(e -> e.getCode()).collect(Collectors.joining (", "));
        String allowedHeadersStr = String.join(", ", allowedHeaders);
        response.addHeader(new Http2Header(Http2HeaderName.ACCESS_CONTROL_ALLOW_METHODS, allowedMethodsStr));
        response.addHeader(new Http2Header(Http2HeaderName.ACCESS_CONTROL_ALLOW_HEADERS, allowedHeadersStr));
        if(exposeTheseResponseHeadersToBrowser.size() > 0) {
            String exposeStr = String.join(", ", exposeTheseResponseHeadersToBrowser);
            response.addHeader( new Http2Header(Http2HeaderName.ACCESS_CONTROL_EXPOSE_HEADERS, exposeStr));
        }
        response.addHeader(new Http2Header(Http2HeaderName.ACCESS_CONTROL_MAX_AGE, maxAgeSeconds+""));
        response.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, "0"));

        sendResponse(responseStream, response);
    }

    private void send403Response(ResponseStreamHandle responseStream, Http2Request request) {
        Http2Response response = new Http2Response();
        response.addHeader(new Http2Header(Http2HeaderName.STATUS, "403"));
        response.addHeader(new Http2Header(Http2HeaderName.VARY, "Origin"));
        sendResponse(responseStream, response);
    }

    private void sendResponse(ResponseStreamHandle responseStream, Http2Response response) {
        CompletableFuture<StreamWriter> process = responseStream.process(response);
        try {
            process.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw SneakyThrow.sneak(e);
        }
    }

}
