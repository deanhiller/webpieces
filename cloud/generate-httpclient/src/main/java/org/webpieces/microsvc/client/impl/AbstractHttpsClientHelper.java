package org.webpieces.microsvc.client.impl;

import com.orderlyhealth.api.Endpoint;
import com.orderlyhealth.api.OrderlyHeaders;
import com.orderlyhealth.api.json.BadCustomerRequestException;
import com.orderlyhealth.api.monitoring.OrderlyMonitoring;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.highlevel.Http2Response;
import com.webpieces.http2.api.dto.lowlevel.CancelReason;
import com.webpieces.http2.api.dto.lowlevel.Http2Method;
import com.webpieces.http2.api.dto.lowlevel.StatusCode;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import com.webpieces.http2.api.dto.lowlevel.lib.StreamMsg;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.data.api.DataWrapperGenerator;
import org.webpieces.data.api.DataWrapperGeneratorFactory;
import org.webpieces.http2client.api.Http2Client;
import org.webpieces.http2client.api.Http2Socket;
import org.webpieces.http2client.api.Http2SocketListener;
import org.webpieces.http2client.api.dto.FullRequest;
import org.webpieces.http2client.api.dto.FullResponse;
import org.webpieces.plugin.json.JacksonJsonConverter;
import org.webpieces.router.api.RouterResponseHandler;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.futures.FutureHelper;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class AbstractHttpsClientHelper {

    private static final Logger log = LoggerFactory.getLogger(AbstractHttpsClientHelper.class);

    protected static final DataWrapperGenerator WRAPPER_GEN = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    protected JacksonJsonConverter jsonMapper;
    protected Http2Client client;
    protected ScheduledExecutorService schedulerSvc;
    private OrderlyMonitoring monitoring;

    private FutureHelper futureUtil;

    public AbstractHttpsClientHelper(JacksonJsonConverter jsonMapper,
                                     Http2Client client,
                                     FutureHelper futureUtil,
                                     ScheduledExecutorService schedulerSvc,
                                     OrderlyMonitoring monitoring) {

        this.jsonMapper = jsonMapper;
        this.client = client;
        this.futureUtil = futureUtil;
        this.schedulerSvc = schedulerSvc;
        this.monitoring = monitoring;

        log.info("USING keyStoreLocation=" + getKeyStoreLocation());

    }

    protected abstract String getKeyStoreLocation();

    protected abstract String getKeyStorePassword();

    public StreamRef stream(ResponseStreamHandle handleOrig, Endpoint endpoint, List<Http2Header> extraHeaders) {

        RouterResponseHandler serverResponseHandle = (RouterResponseHandler) handleOrig;
        Http2Request originalRequest = Current.request().originalRequest;

        InetSocketAddress apiAddress = endpoint.getApiAddress();
        String path = endpoint.getPath();
        Http2Request httpReq = createHttpReq(apiAddress, "POST", path, extraHeaders);

        Http2Header len = originalRequest.getHeaderLookupStruct().getHeader(Http2HeaderName.CONTENT_LENGTH);
        Http2Header encoding = originalRequest.getHeaderLookupStruct().getHeader(Http2HeaderName.TRANSFER_ENCODING);

        if (len != null) {
            httpReq.addHeader(len);
        } else if (encoding != null) {
            httpReq.addHeader(encoding);
        } else {
            throw new IllegalArgumentException("Missing header Content-Length or Transfer-Encoding");
        }

        com.orderlyhealth.json.client.util.CloseListener closeListener = new com.orderlyhealth.json.client.util.CloseListener(serverResponseHandle);
        Http2Socket httpSocket = createSocket(apiAddress, closeListener);
        CompletableFuture<Void> connect = httpSocket.connect(apiAddress);

        String curl = createCurl2(apiAddress.getPort(), httpReq, () -> "");
        log.info("curl request (BUT WITHOUT the data)socket(" + httpSocket + ")=\n" + curl);

        RequestStreamHandle stream = httpSocket.openStream();

        CompletableFuture<StreamRef> aFutureStreamRef = new CompletableFuture<>();
        CompletableFuture<StreamWriter> writer = connect.thenCompose(s -> {

            Runnable cancelFunc = () -> cancel(httpSocket);
            StreamRef ref = stream.process(httpReq, new ProxyHandle(serverResponseHandle, cancelFunc));

            aFutureStreamRef.complete(ref);

            Runnable serverCloseFun = () -> serverResponseHandle.closeSocket("Connection to orderly server went down");

            return ref.getWriter().thenApply(w -> new ProxyWriter(w, serverCloseFun, true));

        });

        return new com.orderlyhealth.json.client.util.ClientStreamRef(writer, aFutureStreamRef);

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

    private class ProxyHandle implements ResponseStreamHandle {

        private RouterResponseHandler handle;
        private Runnable cancelFunc;

        public ProxyHandle(RouterResponseHandler handle, Runnable cancelFunc) {
            this.handle = handle;
            this.cancelFunc = cancelFunc;
        }

        @Override
        public CompletableFuture<StreamWriter> process(Http2Response response) {
            log.info("received response=" + response);
            return handle.process(response).thenApply(w -> new ProxyWriter(w, cancelFunc, false));
        }

        @Override
        public PushStreamHandle openPushStream() {
            return handle.openPushStream();
        }

        @Override
        public CompletableFuture<Void> cancel(CancelReason reason) {
            log.info("cancel reason=" + reason);
            return handle.cancel(reason);
        }

    }

    private class ProxyWriter implements StreamWriter {

        private StreamWriter writer;
        private Runnable cancelFunc;
        private boolean isClientWriter;
        private boolean hadException;

        public ProxyWriter(StreamWriter writer, Runnable cancelFunc, boolean isClientWriter) {
            this.writer = writer;
            this.cancelFunc = cancelFunc;
            this.isClientWriter = isClientWriter;
        }

        @Override
        public CompletableFuture<Void> processPiece(StreamMsg data) {

            if (hadException) {
                return CompletableFuture.completedFuture(null); //no need to continue if there was an exception
            }

            CompletableFuture<Void> future = new CompletableFuture<>();

            try {
                future = writer.processPiece(data);
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }

            return future.exceptionally(t -> {

                hadException = true;

                if (t instanceof NioClosedChannelException) {
                    log.info("Remote end closed socket while we were processing. clientWriter(writes to remote orderlyserver)=" + isClientWriter);
                } else {
                    log.error("Exception sending response chunk. clientWriter(writes to remote orderlyserver)=" + isClientWriter, t);
                }

                //In the case of closed exception AND backup pressure, we will NOT receive a farEndClosed soooo, we basically
                //close the client down since server is closed(isClientWriter=true)
                cancelFunc.run();

                return null;

            });

        }

    }

    public Http2Request createHttpReq(InetSocketAddress apiAddress, String method, String path, List<Http2Header> extraHeaders) {

        Http2Request httpReq = new Http2Request();

        httpReq.addHeader(new Http2Header(Http2HeaderName.METHOD, method));
        httpReq.addHeader(new Http2Header(Http2HeaderName.AUTHORITY, apiAddress.getHostString()));
        httpReq.addHeader(new Http2Header(Http2HeaderName.PATH, path));
        httpReq.addHeader(new Http2Header(Http2HeaderName.USER_AGENT, "Orderly GCP Requestor"));
        httpReq.addHeader(new Http2Header(Http2HeaderName.ACCEPT, "application/json"));//Http2HeaderName.ACCEPT
        httpReq.addHeader(new Http2Header(Http2HeaderName.CONTENT_TYPE, "application/json"));

        //GCP vs. OrderlyInternal Client vs. OrderlyExternalClient vs. Circle CI client ALL need different extra
        //headers in their requests...
        for (Http2Header header : extraHeaders) {
            httpReq.addHeader(header);
        }

        return httpReq;

    }

    /**
     * <b>DO NOT USE FOR PUBLIC HTTP REQUEST THIS IS FOR INTERNAL USE ONLY</b>
     */
    public <T> CompletableFuture<T> sendHttpRequest(Method method, Object request, Endpoint endpoint, Class<T> responseType, List<Http2Header> extraHeaders) {

        if (extraHeaders == null) {
            extraHeaders = new ArrayList<>();
        }

        InetSocketAddress apiAddress = endpoint.getApiAddress();
        String httpMethod = endpoint.getHttpMethod();
        String endpointPath = endpoint.getPath();
        Http2Request httpReq = createHttpReq(apiAddress, httpMethod, endpointPath, extraHeaders);
        com.orderlyhealth.json.client.util.RequestCloseListener closeListener = new com.orderlyhealth.json.client.util.RequestCloseListener(schedulerSvc);
        Http2Socket httpSocket = createSocket(apiAddress, closeListener);
        CompletableFuture<Void> connect = httpSocket.connect(apiAddress);

        String jsonRequest = marshal(request);
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

        RequestContext context = Current.getContext();

        Map<String, String> ctxMap = MDC.getCopyOfContextMap();

        if (ctxMap == null) {
            ctxMap = new HashMap<>();
        }

        Map<String, String> mapCopy = ctxMap;

        long start = System.currentTimeMillis();
        CompletableFuture<T> future = futureUtil.catchBlockWrap(
                () -> sendAndTranslate(mapCopy, apiAddress, responseType, httpSocket, connect, fullRequest, context, jsonRequest),
                (t) -> translateException(httpReq, t)
        );

        // Track metrics with future.handle()
        // If method is null, then no need to track metrics
        // If monitoring is null, then this call probably came from OrderlyTest
        if (method != null && monitoring != null) {
            future = future.handle((r, e) -> {
                String clientId = context.getRequest().getRequestState(OrderlyHeaders.CLIENT_ID.getHeaderName());
                monitoring.endHttpClientTimer(method, clientId, endpoint, start);

                if (e != null) {
                    monitoring.incrementHttpClientExceptionMetric(method, clientId, endpoint, e.getClass().getSimpleName());
                    return CompletableFuture.<T>failedFuture(e);
                }

                monitoring.incrementHttpClientSuccessMetric(method, clientId, endpoint);
                return CompletableFuture.completedFuture(r);
            }).thenCompose(Function.identity());
        }

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

    private <T> CompletableFuture<T> sendAndTranslate(Map<String, String> ctxMap, InetSocketAddress apiAddress, Class<T> responseType, Http2Socket httpSocket, CompletableFuture<Void> connect, FullRequest fullRequest, RequestContext context, String jsonReq) {
        return connect
                .thenCompose(voidd -> httpSocket.send(fullRequest))
                .thenApply(fullResponse -> unmarshal(jsonReq, ctxMap, context, fullRequest, fullResponse, apiAddress.getPort(), responseType));
    }

    protected Http2Socket createSocket(InetSocketAddress apiAddress, Http2SocketListener listener) {

        SSLEngine engine = createEngine(apiAddress.getHostName(), apiAddress.getPort());

        return client.createHttpsSocket(engine, listener);

    }

    public SSLEngine createEngine(String host, int port) {

        try {

            InputStream in = this.getClass().getResourceAsStream(getKeyStoreLocation());

            if (in == null) {
                throw new IllegalStateException("keyStoreLocation=" + getKeyStoreLocation() + " was not found on classpath");
            }

            //char[] passphrase = password.toCharArray();
            // First initialize the key and trust material.
            KeyStore ks = KeyStore.getInstance("JKS");
            SSLContext sslContext = SSLContext.getInstance("TLS");

            ks.load(in, getKeyStorePassword().toCharArray());

            //****************Client side specific*********************

            // TrustManager's decide whether to allow connections.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");

            tmf.init(ks);

            sslContext.init(null, tmf.getTrustManagers(), null);

            //****************Client side specific*********************

            SSLEngine engine = sslContext.createSSLEngine(host, port);

            engine.setUseClientMode(true);

            return engine;

        } catch (Exception ex) {
            throw new RuntimeException("Could not create SSLEngine", ex);
        }

    }

    private <T> T unmarshal(String jsonReq, Map<String, String> ctxMap, RequestContext ctx, FullRequest request, FullResponse httpResp, int port, Class<T> type) {

        for (Map.Entry<String, String> entry : ctxMap.entrySet()) {
            MDC.put(entry.getKey(), entry.getValue());
        }

        //SINCE 99% of the time, we don't change threads on executing resolution of a future, we can set the ThreadLocal
        //for the RequestContext here to transfer that info to this thread AFTER a remote server responds
        //Of course, we have no way of resetting it, but the platform does initialize it on every request and every
        //client sets it like this too so we should be good
        Current.setContext(ctx);

        DataWrapper payload = httpResp.getPayload();
        String contents = payload.createStringFromUtf8(0, payload.getReadableSize());
        String url = "https://" + request.getHeaders().getAuthority() + request.getHeaders().getPath();

        log.info("unmarshalling response json='" + contents + "' http=" + httpResp.getHeaders() + " from request="+jsonReq+" to " + url);

        if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_200_OK) {
            if (type == null) {
                return null;
            }
            return unmarshalJson(type, contents);
        }

        String message = "\njson error='" + contents + "' fullResp=" + httpResp + " url='" + url + "' originalRequest="+jsonReq;

        if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_400_BADREQUEST) {
            //MUST translate to http 500 for this server.  ie. If the downstream servers tell US that
            //we sent a bad request, we have a bug.  Either 1. We did not guard our clients from the
            //bad request first sending our clients a 400 OR we simply filled out the request wrong
            throw new InternalErrorException("This server sent a bad request to a down stream server." + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_401_UNAUTHORIZED) {
            throw new AuthenticationException("Unauthorized " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_403_FORBIDDEN) {
            throw new ForbiddenException("Forbidden " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_500_INTERNAL_SVR_ERROR) {
            throw new BadGatewayException("Bad Gateway " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_502_BAD_GATEWAY) {
            throw new BadGatewayException("Bad Gateway " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_504_GATEWAY_TIMEOUT) {
            throw new GatewayTimeoutException("Gateway Timeout " + message);
        } else if (httpResp.getHeaders().getStatus() == 429) {
            throw new TooManyRequestsException("Exceeded Rate " + message);
        } else if (httpResp.getHeaders().getStatus() == 491) {
            throw new BadCustomerRequestException("Bad customer request");
        } else {
            throw new InternalErrorException("\nRUN the curl request above to test this error!!!\n" + message);
        }

    }

    private <T> T unmarshalJson(Class<T> type, String contents) {
        return jsonMapper.readValue(contents, type);
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

    private String  marshal(Object authRequest) {

        try {
            //string comes in handy for debugging!!!
            return jsonMapper.writeValueAsString(authRequest);

        } catch (Exception ex) {
            throw new RuntimeException("Bug in marshalling to json=" + authRequest, ex);
        }

    }

}
