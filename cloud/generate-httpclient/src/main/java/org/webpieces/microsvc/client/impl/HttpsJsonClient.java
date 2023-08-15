package org.webpieces.microsvc.client.impl;

import com.google.inject.Inject;
import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.Http2Method;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
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
import org.webpieces.microsvc.client.api.HttpsConfig;
import org.webpieces.nio.api.channels.HostWithPort;
import org.webpieces.plugin.json.JacksonJsonConverter;
import org.webpieces.plugin.json.JsonError;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.futures.FutureHelper;

import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.util.*;

import org.webpieces.util.futures.XFuture;
import org.webpieces.util.security.Masker;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Singleton
public class HttpsJsonClient {

    private static final Logger log = LoggerFactory.getLogger(HttpsJsonClient.class);

    protected static final DataWrapperGenerator WRAPPER_GEN = DataWrapperGeneratorFactory.createDataWrapperGenerator();

    protected static final int UNSECURE_PORT = 80;
    protected static final int SECURE_PORT = 443;
    private final String serversName;
    private final MeterRegistry metrics;

    private HttpsConfig httpsConfig;
    protected JacksonJsonConverter jsonMapper;
    protected Http2Client client;
    protected ScheduledExecutorService schedulerSvc;
    private FutureHelper futureUtil;
    private final Set<String> secureList = new HashSet<>();
    private final Set<PlatformHeaders> transferHeaders = new HashSet<>();

    private final ConcurrentMap<String, AtomicInteger> clientToCounter = new ConcurrentHashMap<>();

    private Masker masker;

    @Inject
    public HttpsJsonClient(
            HttpsConfig httpsConfig,
            ClientServiceConfig clientServiceConfig,
            JacksonJsonConverter jsonMapper,
            Http2Client client,
            FutureHelper futureUtil,
            ScheduledExecutorService schedulerSvc,
            Masker masker,
            MeterRegistry metrics
    ) {
        if(clientServiceConfig.getHcl() == null)
            throw new IllegalArgumentException("clientServiceConfig.getHcl() cannot be null and was");

        this.serversName = clientServiceConfig.getServersName();
        this.metrics = metrics;

        List<PlatformHeaders> listHeaders = clientServiceConfig.getHcl().listHeaderCtxPairs();

        this.httpsConfig = httpsConfig;
        this.jsonMapper = jsonMapper;
        this.client = client;
        this.futureUtil = futureUtil;
        this.schedulerSvc = schedulerSvc;
        this.masker = masker;

        Context.checkForDuplicates(listHeaders);

        for(PlatformHeaders header : listHeaders) {
            if(header.isSecured()) {
                secureList.add(header.getHeaderName());
            }
            //perhaps header needs to be transferred too...
            if(header.isWantTransferred()) {
                transferHeaders.add(header);
            }
        }

        log.info("USING keyStoreLocation=" + httpsConfig.getKeyStoreLocation());
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

        for(PlatformHeaders header : transferHeaders) {
            String magic = Context.getMagic(header);
            if(magic != null)
                httpReq.addHeader(new Http2Header(header.getHeaderName(), magic));
        }

        return httpReq;
    }

    /**
     * <b>DO NOT USE FOR PUBLIC HTTP REQUEST THIS IS FOR INTERNAL USE ONLY</b>
     */
    public <T> XFuture<T> sendHttpRequest(Method method, Object request, Endpoint endpoint, Class<T> responseType, boolean forHttp) {

        HostWithPort apiAddress = endpoint.getServerAddress();
        String httpMethod = endpoint.getHttpMethod();
        String endpointPath = endpoint.getUrlPath();
        Http2Request httpReq = createHttpReq(apiAddress, httpMethod, endpointPath);
        RequestCloseListener closeListener = new RequestCloseListener(schedulerSvc);
        Http2Socket httpSocket = createSocket(apiAddress, closeListener, forHttp);
        XFuture<Void> connect = httpSocket.connect(apiAddress);

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

        log.info("curl request on socket(" + httpSocket + ")" + createCurl(fullRequest));

        Map<String, Object> fullContext = Context.getContext();
        if(fullContext == null) {
            throw new IllegalStateException("Missing webserver filters? Context.getContext() must contain data");
        }

        XFuture<T> future = futureUtil.catchBlockWrap(
                () -> sendAndTranslate(method, apiAddress, responseType, httpSocket, connect, fullRequest, jsonRequest),
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

    private <T> XFuture<T> sendAndTranslate(Method method, HostWithPort apiAddress, Class<T> responseType, Http2Socket httpSocket, XFuture<Void> connect, FullRequest fullRequest, String jsonReq) {
        String clientsName = method.getDeclaringClass().getSimpleName();
        String methodName = method.getName();

        Iterable<Tag> tags = List.of(
                Tag.of("api", clientsName),
                Tag.of("method", methodName)
        );
        AtomicInteger inFlightCounter = clientToCounter.compute(clientsName, (k, v) -> computeLazyToAvoidOOM(k, v, tags));

        return connect
                .thenCompose(voidd -> send(tags, inFlightCounter, httpSocket, fullRequest))
                .thenApply(fullResponse -> unmarshal(jsonReq, fullRequest, fullResponse, apiAddress.getPort(), responseType));
    }

    //Creating a gauage in micrometer will have micrometer thread keep a reference to object forever.
    //It is very important to do this lazy or we add a new AtomicInteger on every api call if it
    //already exists instead of ONCE PR API CLASS (we do not even do once per method yet, though we could)
    public AtomicInteger computeLazyToAvoidOOM(String key, AtomicInteger existing, Iterable<Tag> tags) {
        if(existing == null) {
            existing = new AtomicInteger(0);
            metrics.gauge("webpieces.requests.inflight", tags, existing, (c) -> c.get());
        }
        return existing;
    }

    public XFuture<FullResponse> send(Iterable<Tag> tags, AtomicInteger inFlightCounter, Http2Socket socket, FullRequest request) {
        XFuture<FullResponse> send = socket.send(request);

        //in an ideal world, we would use the async send which can notify us when 'write' is done so it is out the door, but
        //we never know when read is done until we parse the whole message (not exactly symmetrical)
        Counter requestCounter = metrics.counter("webpieces.requests", tags);
        requestCounter.increment();
        inFlightCounter.incrementAndGet();

        return send.thenApply( (response) -> {
            Counter responseCounter = metrics.counter("webpieces.responses", tags);
            responseCounter.increment();
            inFlightCounter.decrementAndGet();

            return response;
        });
    }

    protected Http2Socket createSocket(HostWithPort apiAddress, Http2SocketListener listener, boolean forHttp) {
        if(forHttp) {
            return client.createHttpSocket(listener);
        }

        SSLEngine engine = createEngine(apiAddress.getHostOrIpAddress(), apiAddress.getPort());
        return client.createHttpsSocket(engine, listener);
    }

    public SSLEngine createEngine(String host, int port) {

        try {
            String keyStoreType = "JKS";
            if(httpsConfig.getKeyStoreLocation().endsWith(".p12")) {
                keyStoreType = "PKCS12";
            }

            URL resource = this.getClass().getClassLoader().getResource(httpsConfig.getKeyStoreLocation());


            InputStream in = this.getClass().getResourceAsStream(httpsConfig.getKeyStoreLocation());

            if (in == null) {
                throw new IllegalStateException("keyStoreLocation=" + httpsConfig.getKeyStoreLocation() + " was not found on classpath");
            }

            //char[] passphrase = password.toCharArray();
            // First initialize the key and trust material.
            KeyStore ks = KeyStore.getInstance(keyStoreType);
            SSLContext sslContext = SSLContext.getInstance("TLS");

            ks.load(in, httpsConfig.getKeyStorePassword().toCharArray());

            //****************Client side specific*********************

            // TrustManager's decide whether to allow connections.
            TrustManagerFactory tmf = TrustManagerFactory.getInstance("PKIX");

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

    private <T> T unmarshal(String jsonReq, FullRequest request, FullResponse httpResp, int port, Class<T> type) {

        DataWrapper payload = httpResp.getPayload();
        String contents = payload.createStringFromUtf8(0, payload.getReadableSize());
        String url = "https://" + request.getHeaders().getAuthority() + request.getHeaders().getPath();

        log.info("unmarshalling response json='" + contents + "' http=" + httpResp.getHeaders() + " from request="+jsonReq+" to " + url);

        if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_200_OK) {
            if (type == null || Void.class == type) {
                return null;
            }
            return unmarshalJson(type, contents);
        }

        JsonError errorIfCanRead = failOpenForSomeServices(contents);
        if(errorIfCanRead != null) {
            errorIfCanRead.getServiceFailureChain().add(0, serversName);
        }

        String message = formMessage(contents, httpResp, url, jsonReq, errorIfCanRead);

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
            throw new BadGatewayException(message, errorIfCanRead);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_502_BAD_GATEWAY) {
            throw new BadGatewayException(message, errorIfCanRead);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_504_GATEWAY_TIMEOUT) {
            throw new GatewayTimeoutException("Gateway Timeout " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_429_TOO_MANY_REQUESTS) {
            throw new TooManyRequestsException("Exceeded Rate " + message);
        } else if (httpResp.getHeaders().getKnownStatusCode() == StatusCode.HTTP_491_BAD_CUSTOMER_REQUEST) {
            throw new BadCustomerRequestException(errorIfCanRead != null ? errorIfCanRead.getError() : "Bad customer request");
        } else {
            throw new InternalServerErrorException("\nRUN the curl request above to test this error!!!\n" + message);
        }

    }

    private String formMessage(String contents, FullResponse httpResp, String url, String jsonReq, JsonError errorIfCanRead) {
        if(errorIfCanRead == null) {
            return "\nresponse body='" + contents + "'" +
                    "\nfullResp=" + httpResp +
                    "\nurl='" + url + "'" +
                    "\noriginalRequestBody=" + jsonReq;
        }

        String serviceWithError = errorIfCanRead.getServiceWithError();
        String message = serviceWithError+" had an error. msg="+errorIfCanRead.getError()+"  Full svc chain="+errorIfCanRead.getServiceFailureChain()+
                "\nfullResp=" + httpResp +
                "\nurl='" + url + "'" +
                "\noriginalRequestBody=" + jsonReq;

        return message;
    }

    private JsonError failOpenForSomeServices(String contents) {
        try {
            return unmarshalJson(JsonError.class, contents);
        } catch (Throwable e) {
            //silently fail
            log.trace("Failed unmarshalling(incompatible service)", e);
            return null;
        }
    }

    private <T> T unmarshalJson(Class<T> type, String contents) {
        return jsonMapper.readValue(contents, type);
    }

    private String createCurl(FullRequest request) {

        DataWrapper data = request.getPayload();
        String body = data.createStringFromUtf8(0, data.getReadableSize());
        Http2Request req = request.getHeaders();

        return createCurl2(req, () -> ("--data '" + body + "'"));

    }


    private String createCurl2(Http2Request req, Supplier<String> supplier) {

        String s = "";

        s += "\n\n************************************************************\n";
        s += "            CURL REQUEST\n";
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

    private String  marshal(Object request) {

        try {
            //string comes in handy for debugging!!!
            return jsonMapper.writeValueAsString(request);

        } catch (Exception ex) {
            throw new RuntimeException("Bug in marshalling to json=" + request, ex);
        }

    }

}
