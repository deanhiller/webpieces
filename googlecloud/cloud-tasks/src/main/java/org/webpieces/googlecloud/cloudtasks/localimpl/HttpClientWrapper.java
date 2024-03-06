package org.webpieces.googlecloud.cloudtasks.localimpl;

import com.google.inject.Inject;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.Http2Method;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http.StatusCode;
import org.webpieces.http.exception.*;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.microsvc.client.api.ClientSSLEngineFactory;
import org.webpieces.microsvc.server.api.HeaderTranslation;
import org.webpieces.util.HostWithPort;
import org.webpieces.util.security.Masker;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.util.futures.XFuture;

import javax.inject.Singleton;
import javax.net.ssl.SSLEngine;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

@Singleton
public class HttpClientWrapper {

    private static final Logger log = LoggerFactory.getLogger(HttpClientWrapper.class);

    protected static final DataWrapperGenerator WRAPPER_GEN = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    protected static final int UNSECURE_PORT = 80;
    protected static final int SECURE_PORT = 443;

    private ClientSSLEngineFactory sslFactory;
    protected Http2Client client;
    protected ScheduledExecutorService schedulerSvc;

    private FutureHelper futureUtil;

    private final Set<String> secureList = new HashSet<>();
    private final Set<PlatformHeaders> toTransfer = new HashSet<>();

    private Masker masker;

    @Inject
    public HttpClientWrapper(
            ClientSSLEngineFactory sslFactory,
            Http2Client client,
            FutureHelper futureUtil,
            Masker masker,
            HeaderTranslation translation
    ) {
        this.sslFactory = sslFactory;
        this.client = client;
        this.futureUtil = futureUtil;
        this.masker = masker;

        List<PlatformHeaders> listHeaders = translation.getHeaders();

        for(PlatformHeaders header : listHeaders) {
            if(header.isSecured())
                secureList.add(header.getHeaderName());
            if(header.isWantTransferred())
                toTransfer.add(header);
        }
    }

    private void cancel(Http2Socket clientSocket) {

        try {

            clientSocket.close().exceptionally(t -> {
                log.error("Error closing client socket", t);
                return null;
            });

        } catch (NioClosedChannelException ex) {
            log.info("channel already closed.");
        }

    }

    public Http2Request createHttpReq(HostWithPort apiAddress, String method, String path) {

        Http2Request httpReq = new Http2Request();

        httpReq.addHeader(new Http2Header(Http2HeaderName.METHOD, method));
        httpReq.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, apiAddress.getHostOrIpAddress()+":"+apiAddress.getPort()));
        httpReq.addHeader(new Http2Header(Http2HeaderName.PATH, path));
        httpReq.addHeader(new Http2Header(Http2HeaderName.USER_AGENT, "Webpieces Generated API Client"));
        httpReq.addHeader(new Http2Header(Http2HeaderName.ACCEPT, "application/json"));//Http2HeaderName.ACCEPT
        httpReq.addHeader(new Http2Header(Http2HeaderName.CONTENT_TYPE, "application/json"));

        for(PlatformHeaders header : toTransfer) {
            String magic = Context.getMagic(header);
            if(magic != null)
                httpReq.addHeader(new Http2Header(header.getHeaderName(), magic));
        }

        return httpReq;
    }

    /**
     * <b>DO NOT USE FOR PUBLIC HTTP REQUEST THIS IS FOR INTERNAL USE ONLY</b>
     */
    public XFuture<String> sendHttpRequest(String jsonRequest, Endpoint endpoint) {

        HostWithPort apiAddress = endpoint.getServerAddress();
        String httpMethod = endpoint.getHttpMethod();
        String endpointPath = endpoint.getUrlPath();
        Http2Request httpReq = createHttpReq(apiAddress, httpMethod, endpointPath);
        RequestCloseListener closeListener = new RequestCloseListener(schedulerSvc);
        Http2Socket httpSocket = createSocket(apiAddress, closeListener);
        XFuture<Void> connect = httpSocket.connect(apiAddress);

        byte[] reqAsBytes = jsonRequest.getBytes(StandardCharsets.UTF_8);
        if (jsonRequest.equals("null")) { // hack
            reqAsBytes = new byte[0];
        }
        DataWrapper data = WRAPPER_GEN.wrapByteArray(reqAsBytes);

        if (httpReq.getKnownMethod() == Http2Method.POST) {
            httpReq.addHeader(new Http2Header(Http2HeaderName.CONTENT_LENGTH, String.valueOf(data.getReadableSize())));
        }
        httpReq.addHeader(new Http2Header(Http2HeaderName.SCHEME, "https"));

        FullRequest fullRequest = new FullRequest(httpReq, data, null);

        log.info("curl request on socket(" + httpSocket + ")" + createCurl(fullRequest));

        Map<String, Object> fullContext = Context.getContext();
        if(fullContext == null) {
            throw new IllegalStateException("Missing webserver filters? Context.getFullContext() must contain data");
        }

        XFuture<String> future = futureUtil.catchBlockWrap(
                () -> sendAndTranslate(apiAddress, httpSocket, connect, fullRequest, jsonRequest),
                (t) -> translateException(httpReq, t)
        );

//        // Track metrics with future.handle()
//        // If method is null, then no need to track metrics
//        // If monitoring is null, then this call probably came from OrderlyTest
//        if (method != null && monitoring != null) {
//            future = future.handle((r, e) -> {
//                String clientId = context.getRequest().getRequestState(OrderlyHeaders.CLIENT_ID.getHeaderName());
//                monitoring.endHttpClientTimer(method, clientId, endpoint, start);
//
//                if (e != null) {
//                    monitoring.incrementHttpClientExceptionMetric(method, clientId, endpoint, e.getClass().getSimpleName());
//                    return XFuture.<T>failedFuture(e);
//                }
//
//                monitoring.incrementHttpClientSuccessMetric(method, clientId, endpoint);
//                return XFuture.completedFuture(r);
//            }).thenCompose(Function.identity());
//        }

        //so we can cancel the future exactly when the socket closes
        closeListener.setFuture(future);

        return future;

    }

    private Throwable translateException(Http2Request httpReq, Throwable t) {

        if (t instanceof HttpException) {
            return t;
        }

        return new RuntimeException("Exception from downstream client when issuing request=" + httpReq, t);

    }

    private XFuture<String> sendAndTranslate(HostWithPort apiAddress, Http2Socket httpSocket, XFuture<Void> connect, FullRequest fullRequest, String jsonReq) {
        return connect
                .thenCompose(voidd -> httpSocket.send(fullRequest))
                .thenApply(fullResponse -> unmarshal(jsonReq, fullRequest, fullResponse, apiAddress.getPort()));
    }

    protected Http2Socket createSocket(HostWithPort apiAddress, Http2SocketListener listener) {
        SSLEngine engine = sslFactory.createEngine(apiAddress.getHostOrIpAddress(), apiAddress.getPort());
        return client.createHttpsSocket(engine, listener);
        //return client.createHttpsSocket(listener);
    }

    private String unmarshal(String jsonReq, FullRequest request, FullResponse httpResp, int port) {

        DataWrapper payload = httpResp.getPayload();
        String contents = payload.createStringFromUtf8(0, payload.getReadableSize());
        String url = "https://" + request.getHeaders().getAuthority() + request.getHeaders().getPath();

        log.info("unmarshalling response json='" + contents + "' http=" + httpResp.getHeaders() + " from request="+jsonReq+" to " + url);

        if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_200_OK) {
            return contents;
        }

        String message = "\njson error='" + contents + "' fullResp=" + httpResp + " url='" + url + "' originalRequest="+jsonReq;

        if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_400_BAD_REQUEST) {
            //MUST translate to http 500 for this server.  ie. If the downstream servers tell US that
            //we sent a bad request, we have a bug.  Either 1. We did not guard our clients from the
            //bad request first sending our clients a 400 OR we simply filled out the request wrong
            throw new InternalServerErrorException("This server sent a bad request to a down stream server." + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_401_UNAUTHORIZED) {
            throw new UnauthorizedException("Unauthorized " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_403_FORBIDDEN) {
            throw new ForbiddenException("Forbidden " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_500_INTERNAL_SERVER_ERROR) {
            throw new BadGatewayException("Bad Gateway " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_502_BAD_GATEWAY) {
            throw new BadGatewayException("Bad Gateway of Bad Gateway " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_504_GATEWAY_TIMEOUT) {
            throw new GatewayTimeoutException("Gateway Timeout " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_429_TOO_MANY_REQUESTS) {
            throw new TooManyRequestsException("Exceeded Rate " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_491_BAD_CUSTOMER_REQUEST) {
            throw new BadCustomerRequestException(message);
        } else {
            throw new InternalServerErrorException("\nRUN the curl request above to test this error!!!\n" + message);
        }

    }

    private String createCurl(FullRequest request) {

        DataWrapper data = request.getPayload();
        String body = data.createStringFromUtf8(0, data.getReadableSize());
        Http2Request req = request.getHeaders();

        return createCurl2(req, () -> ("--data '" + body + "'"));

    }

    private String createCurl2(Http2Request req, Supplier<String> supplier) {

        String s = "";

        s += "******SENDING simulated request from in-memory queue**************************\n";
        s += "     (requests are a river) "+req.getMethodString()+" "+req.getPath()+" \n";
        s += "***************************************************************\n";

        s += "curl -k --request " + req.getKnownMethod().getCode() + " ";
        for (Http2Header header : req.getHeaders()) {

            if (header.getName().startsWith(":")) {
                continue; //base headers we can discard
            }

            if(secureList.contains(header.getName())) {
                s += "-H \"" + header.getName() + ":" + masker.maskSensitiveData(header.getValue()) + "\" ";
            } else {
                s += "-H \"" + header.getName() + ":" + header.getValue() + "\" ";
            }

        }

        final String hostHeader = (req.getSingleHeaderValue(Http2HeaderName.AUTHORITY).endsWith(":"+UNSECURE_PORT) ||
                req.getSingleHeaderValue(Http2HeaderName.AUTHORITY).endsWith(":"+SECURE_PORT)) ?
                req.getSingleHeaderValue(Http2HeaderName.AUTHORITY).split(":")[0] : req.getSingleHeaderValue(Http2HeaderName.AUTHORITY);

        s += "-H \"" + KnownHeaderName.HOST + ":" + hostHeader + "\" ";

        String host = req.getSingleHeaderValue(Http2HeaderName.AUTHORITY);
        String path = req.getSingleHeaderValue(Http2HeaderName.PATH);

        s += supplier.get();
        s += " \"https://" + host + path + "\"\n";
        s += "***************************************************************\n";

        return s;

    }

    public void init(ScheduledExecutorService executorService) {
        this.schedulerSvc = executorService;
    }
}
