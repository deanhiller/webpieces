package org.webpieces.router.api.routebldr;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.lib.HeaderType;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamWriter;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.ctx.api.OverwritePlatformResponse;
import org.webpieces.ctx.api.RequestContext;
import org.digitalforge.sneakythrow.SneakyThrow;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DefaultCorsProcessor implements ProcessCors {

    private static final Set<String> DEFAULTS = Set.of(
            Http2HeaderName.METHOD.getHeaderName(),
            Http2HeaderName.SCHEME.getHeaderName(),
            Http2HeaderName.PATH.getHeaderName(),
            Http2HeaderName.AUTHORITY.getHeaderName(),
            Http2HeaderName.ORIGIN.getHeaderName());

    private final Set<String> allowedDomains;
    private final Set<String> allowedHeaders;
    private String exposeTheseResponseHeadersToBrowserStr;
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

        this.allowedDomains = allowedDomains.stream().map((s) -> s.toLowerCase()).collect(Collectors.toSet());
        this.allowedHeaders = allowedRequestHeaders.stream().map((s) -> s.toLowerCase()).collect(Collectors.toSet());
        this.isAllowCredsCookies = isAllowCredsCookies;
        if(exposeTheseResponseHeadersToBrowser.size() > 0) {
            exposeTheseResponseHeadersToBrowserStr = String.join(", ", exposeTheseResponseHeadersToBrowser);
        }
    }

    @Override
    public AccessResult isAccessAllowed(RequestContext ctx) {
        Http2Request request = ctx.getRequest().originalRequest;
        Http2Header originHeader = request.getHeaderLookupStruct().getHeader(Http2HeaderName.ORIGIN);
        if(originHeader == null) {
            throw new IllegalStateException("Should only use this for CORS which requires an Origin header");
        } else if(!allowedDomains.contains("*") && !allowedDomains.contains(originHeader.getValue())) {
            return new AccessResult("Domain not allowed");
        }

        //method is allowed since we are here OR else CORSProcessor is not called
        List<Http2Header> headers = request.getHeaders();
        for(Http2Header header : headers) {
            if(DEFAULTS.contains(header.getName())) {
                continue;
            } else if(!isAllowCredsCookies && header.getKnownName() == Http2HeaderName.COOKIE) {
                return new AccessResult("Credentials / Cookies not supported on this CORS request");
            } else if(!allowedHeaders.contains("*") && !allowedHeaders.contains(header.getName().toLowerCase())) {
                return new AccessResult("Header '"+header.getName()+"' not supported on this CORS request");
            }
        }

        ctx.addModifyResponse(new OverwriteForCorsResponse(originHeader));

        return new AccessResult();
    }

    private class OverwriteForCorsResponse implements OverwritePlatformResponse {
        private Http2Header originHeader;

        public OverwriteForCorsResponse(Http2Header originHeader) {
            this.originHeader = originHeader;
        }

        @Override
        public Object modifyOrReplace(Object r) {
            Http2Response response = (Http2Response) r;

            response.addHeader(new Http2Header(Http2HeaderName.ACCESS_CONTROL_ALLOW_ORIGIN, originHeader.getValue()));
            if(allowedDomains.contains("*")) {
                //since all domains, we must tell caches that Origin header in response will vary
                //since it responds with the domain that requested it
                response.addHeader(new Http2Header(Http2HeaderName.VARY, "Origin"));
            }

            //Tell browser with headers it can read for CORS request
            if(exposeTheseResponseHeadersToBrowserStr != null) {
                response.addHeader( new Http2Header(Http2HeaderName.ACCESS_CONTROL_EXPOSE_HEADERS, exposeTheseResponseHeadersToBrowserStr));
            }

            return response;
        }
    }

    @Override
    public void processOptionsCors(Http2Request request, List<HttpMethod> methods, ResponseStreamHandle responseStream) {
        Http2Header originHeader = request.getHeaderLookupStruct().getHeader(Http2HeaderName.ORIGIN);
        if(originHeader == null)
            throw new IllegalStateException("Should only use this for CORS which requires an Origin header");
        else if(!allowedDomains.contains("*") && !allowedDomains.contains(originHeader.getValue())) {
            send403Response(responseStream, request);
            return;
        }

        Http2Response response = new Http2Response();

        Http2Header methodHeader = request.getHeaderLookupStruct().getHeader(Http2HeaderName.ACCESS_CONTROL_REQUEST_METHOD);
        HttpMethod lookup = HttpMethod.lookup(methodHeader.getValue());
        Http2Header headersRequested = request.getHeaderLookupStruct().getHeader(Http2HeaderName.ACCESS_CONTROL_REQUEST_HEADERS);
        if(!methods.contains(lookup)) {
            response.addHeader(new Http2Header(Http2HeaderName.STATUS, "403"));
        } else if(hasInvalidHeader(allowedHeaders, headersRequested)) {
            response.addHeader(new Http2Header(Http2HeaderName.STATUS, "403"));
        } else {
            response.addHeader(new Http2Header(Http2HeaderName.STATUS, "204"));
        }

        response.addHeader(new Http2Header(Http2HeaderName.ACCESS_CONTROL_ALLOW_ORIGIN, originHeader.getValue()));
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
        if(exposeTheseResponseHeadersToBrowserStr != null) {
            response.addHeader( new Http2Header(Http2HeaderName.ACCESS_CONTROL_EXPOSE_HEADERS, exposeTheseResponseHeadersToBrowserStr));
        }
        response.addHeader(new Http2Header(Http2HeaderName.ACCESS_CONTROL_MAX_AGE, maxAgeSeconds+""));
        response.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, "0"));

        sendResponse(responseStream, response);
    }

    private boolean hasInvalidHeader(Set<String> allowedHeaders, Http2Header headersRequested) {
        if(allowedHeaders.contains("*"))
            return false; // all headers allowed

        String headerStr = headersRequested.getValue();
        String[] eachHeader = headerStr.split(",");
        for(String requestedHeader : eachHeader) {
            if(!allowedHeaders.contains(requestedHeader.trim().toLowerCase()))
                return true;
        }

        return false;
    }

    private void send403Response(ResponseStreamHandle responseStream, Http2Request request) {
        Http2Response response = new Http2Response();
        response.addHeader(new Http2Header(Http2HeaderName.STATUS, "403"));
        response.addHeader(new Http2Header(Http2HeaderName.VARY, "Origin"));
        sendResponse(responseStream, response);
    }

    private void sendResponse(ResponseStreamHandle responseStream, Http2Response response) {
        XFuture<StreamWriter> process = responseStream.process(response);
        try {
            process.get(10, TimeUnit.SECONDS);
        } catch (Exception e) {
            throw SneakyThrow.sneak(e);
        }
    }

}
