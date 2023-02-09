package org.webpieces.googlecloud.cloudtasks.localimpl;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.Http2Method;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
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
import org.webpieces.util.context.Context;
import org.webpieces.util.context.Contexts;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Supplier;

@Singleton
public class HttpClientWrapper {

    private static final Logger log = LoggerFactory.getLogger(HttpClientWrapper.class);

    protected static final DataWrapperGenerator WRAPPER_GEN = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    //private HttpsConfig httpsConfig;
    protected Http2Client client;
    protected ScheduledExecutorService schedulerSvc;

    private FutureHelper futureUtil;

    @Inject
    public HttpClientWrapper(
            //HttpsConfig httpsConfig,
             Http2Client client,
             FutureHelper futureUtil
    ) {
        //this.httpsConfig = httpsConfig;

        this.client = client;
        this.futureUtil = futureUtil;

        //log.info("USING keyStoreLocation=" + httpsConfig.getKeyStoreLocation());
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

    public Http2Request createHttpReq(InetSocketAddress apiAddress, String method, String path) {

        Http2Request httpReq = new Http2Request();

        httpReq.addHeader(new Http2Header(Http2HeaderName.METHOD, method));
        httpReq.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, apiAddress.getHostString()));
        httpReq.addHeader(new Http2Header(Http2HeaderName.PATH, path));
        httpReq.addHeader(new Http2Header(Http2HeaderName.USER_AGENT, "Webpieces Generated API Client"));
        httpReq.addHeader(new Http2Header(Http2HeaderName.ACCEPT, "application/json"));//Http2HeaderName.ACCEPT
        httpReq.addHeader(new Http2Header(Http2HeaderName.CONTENT_TYPE, "application/json"));

        Map<String, String> headers = (Map<String, String>) Context.get(Context.HEADERS);
        for(Map.Entry<String, String> entry : headers.entrySet()) {
            httpReq.addHeader(new Http2Header(entry.getKey(), entry.getValue()));
        }

        return httpReq;
    }

    /**
     * <b>DO NOT USE FOR PUBLIC HTTP REQUEST THIS IS FOR INTERNAL USE ONLY</b>
     */
    public XFuture<String> sendHttpRequest(String jsonRequest, Endpoint endpoint) {

        InetSocketAddress apiAddress = endpoint.getServerAddress();
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

        log.info("curl request on socket(" + httpSocket + ")" + createCurl(fullRequest, apiAddress.getPort()));

        Map<String, Object> fullContext = Context.getContext();
        if(fullContext == null) {
            throw new IllegalStateException("Missing webserver filters? Context.getFullContext() must contain data");
        }

        Map<String, String> ctxMap = MDC.getCopyOfContextMap();

        Contexts contexts = new Contexts(ctxMap, fullContext);

        long start = System.currentTimeMillis();
        XFuture<String> future = futureUtil.catchBlockWrap(
                () -> sendAndTranslate(contexts, apiAddress, httpSocket, connect, fullRequest, jsonRequest),
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

    private XFuture<String> sendAndTranslate(Contexts contexts, InetSocketAddress apiAddress, Http2Socket httpSocket, XFuture<Void> connect, FullRequest fullRequest, String jsonReq) {
        return connect
                .thenCompose(voidd -> httpSocket.send(fullRequest))
                .thenApply(fullResponse -> unmarshal(jsonReq, contexts, fullRequest, fullResponse, apiAddress.getPort()));
    }

    protected Http2Socket createSocket(InetSocketAddress apiAddress, Http2SocketListener listener) {
        //SSLEngine engine = createEngine(apiAddress.getHostName(), apiAddress.getPort());
        //return client.createHttpsSocket(engine, listener);
        return client.createHttpSocket(listener);
    }

//    public SSLEngine createEngine(String host, int port) {
//
//        try {
//
//            String keyStoreType = "JKS";
//            if(httpsConfig.getKeyStoreLocation().endsWith(".p12")) {
//                keyStoreType = "PKCS12";
//            }
//
//            InputStream in = this.getClass().getResourceAsStream(httpsConfig.getKeyStoreLocation());
//
//            if (in == null) {
//                throw new IllegalStateException("keyStoreLocation=" + httpsConfig.getKeyStoreLocation() + " was not found on classpath");
//            }
//
//            //char[] passphrase = password.toCharArray();
//            // First initialize the key and trust material.
//            KeyStore ks = KeyStore.getInstance(keyStoreType);
//            SSLContext sslContext = SSLContext.getInstance("TLS");
//
//            ks.load(in, httpsConfig.getKeyStorePassword().toCharArray());
//
//            //****************Client side specific*********************
//
//            // TrustManager's decide whether to allow connections.
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");
//
//            tmf.init(ks);
//
//            sslContext.init(null, tmf.getTrustManagers(), null);
//
//            //****************Client side specific*********************
//
//            SSLEngine engine = sslContext.createSSLEngine(host, port);
//
//            engine.setUseClientMode(true);
//
//            return engine;
//
//        } catch (Exception ex) {
//            throw new RuntimeException("Could not create SSLEngine", ex);
//        }
//
//    }

    private String unmarshal(String jsonReq, Contexts contexts, FullRequest request, FullResponse httpResp, int port) {

        Map<String, String> loggingCtxMap = contexts.getLoggingCtxMap();
        if(loggingCtxMap != null) {
            for (Map.Entry<String, String> entry : loggingCtxMap.entrySet()) {
                MDC.put(entry.getKey(), entry.getValue());
            }
        }
        //SINCE 99% of the time, we don't change threads on executing resolution of a future, we can set the ThreadLocal
        //for the RequestContext here to transfer that info to this thread AFTER a remote server responds
        //Of course, we have no way of resetting it, but the platform does initialize it on every request and every
        //client sets it like this too so we should be good
        Context.restoreContext(contexts.getWebserverContext());

        DataWrapper payload = httpResp.getPayload();
        String contents = payload.createStringFromUtf8(0, payload.getReadableSize());
        String url = "https://" + request.getHeaders().getAuthority() + request.getHeaders().getPath();

        log.info("unmarshalling response json='" + contents + "' http=" + httpResp.getHeaders() + " from request="+jsonReq+" to " + url);

        if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_200_OK) {
            throw new UnsupportedOperationException("fix this later");
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
        } else if (httpResp.getHeaders().getStatus() == 429) {
            throw new TooManyRequestsException("Exceeded Rate " + message);
        } else if (httpResp.getHeaders().getStatus() == 491) {
            throw new BadCustomerRequestException("Bad customer request");
        } else {
            throw new InternalServerErrorException("\nRUN the curl request above to test this error!!!\n" + message);
        }

    }

    private String createCurl(FullRequest request, int port) {

        DataWrapper data = request.getPayload();
        String body = data.createStringFromUtf8(0, data.getReadableSize());
        Http2Request req = request.getHeaders();

        return createCurl2(port, req, () -> ("--data '" + body + "'"));

    }

    private String createCurl2(int port, Http2Request req, Supplier<String> supplier) {

        String s = "";

        s += "\n\n************************************************************\n";
        s += "            CURL REQUEST\n";
        s += "***************************************************************\n";

        s += "curl -k --request " + req.getKnownMethod().getCode() + " ";
        for (Http2Header header : req.getHeaders()) {

            if (header.getName().startsWith(":")) {
                continue; //base headers we can discard
            }

            s += "-H \"" + header.getName() + ":" + header.getValue() + "\" ";

        }

        String host = req.getSingleHeaderValue(Http2HeaderName.AUTHORITY);
        String path = req.getSingleHeaderValue(Http2HeaderName.PATH);

        s += supplier.get();
        s += " \"https://" + host + ":" + port + path + "\"\n";
        s += "***************************************************************\n";

        return s;

    }

    public void init(ScheduledExecutorService executorService) {
        this.schedulerSvc = executorService;
    }
}
