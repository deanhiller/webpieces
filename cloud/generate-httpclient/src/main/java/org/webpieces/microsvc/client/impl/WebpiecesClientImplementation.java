package org.webpieces.microsvc.client.impl;

import com.orderlyhealth.api.Endpoint;
import com.orderlyhealth.api.Path;
import com.orderlyhealth.api.util.HttpMethod;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.streaming.ResponseStreamHandle;
import com.webpieces.http2.api.streaming.StreamRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.plugin.hibernate.Em;

import javax.persistence.EntityManager;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CompletableFuture;

class WebpiecesClientImplementation implements InvocationHandler {

    private final Logger log = LoggerFactory.getLogger(WebpiecesClientImplementation.class);
    private final com.orderlyhealth.json.client.util.HttpsClientHelper clientHelper;
    private InetSocketAddress addr;
    private com.orderlyhealth.json.client.util.InternalAddHeaders addHeaders;

    public WebpiecesClientImplementation(com.orderlyhealth.json.client.util.HttpsClientHelper clientHelper, InetSocketAddress addr, com.orderlyhealth.json.client.util.InternalAddHeaders addHeaders) {
        this.clientHelper = clientHelper;
        this.addr = addr;
        this.addHeaders = addHeaders;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        EntityManager em = Em.get();

        if(em != null) {
            throw new IllegalStateException("You should never make remote calls while in a transaction");
        }
        else if(args.length != 1) {
            throw new IllegalArgumentException("ALL clients should have EXACTLY 1 argument.  no more, no less.  Read GRPC as to why they do this too!!!");
        }

        Path annotation = method.getAnnotation(Path.class);

        if(annotation == null) {
            throw new IllegalArgumentException("The @Path annotation is missing from method=" + method+" clazz="+method.getDeclaringClass());
        }

        String path = annotation.value();
        HttpMethod httpMethod = annotation.method();
        Class<?> clazz = method.getReturnType();

        if(StreamRef.class.equals(clazz)) {
            return invokeStreaming(proxy, method, args, path);
        }
        else if(!(CompletableFuture.class.isAssignableFrom(clazz))) {
            throw new IllegalStateException("All api methods must return a CompletableFuture");
        }

        ParameterizedType t = (ParameterizedType)method.getGenericReturnType();
        Class retType = (Class)t.getActualTypeArguments()[0];
        RequestContext requestCtx = Current.getContext();

        if(requestCtx == null) {
//            Example:
//            RouterRequest rtrRq = new RouterRequest();
//            rtrRq.requestState.put(OrderlyHeaders.CLIENTID.getHeaderName(), "orderly");
//            rtrRq.requestState.put(OrderlyHeaders.TRANSACTION_ID.getHeaderName(), UUID.randomUUID());
//            RequestContext ctx = new RequestContext(null, null, null, rtrRq, null);
//            Current.setContext(ctx);
            throw new IllegalStateException("Please call Current.setContext with the request context or create your own context");
        }

        log.info("Sending http request to: " + addr.getHostName() + path);

        Endpoint endpoint = new Endpoint(addr, httpMethod.getMethod(), path);
        List<Http2Header> extraHeaders = addHeaders.addHeaders(addr);

        return clientHelper.sendHttpRequest(method, args[0], endpoint, retType, extraHeaders)
            .thenApply(retVal -> {
                Current.setContext(requestCtx);
                return retVal;
            });

    }

    private Object invokeStreaming(Object proxy, Method method, Object[] args, String path) {

        Endpoint endpoint = new Endpoint(addr, "POST", path);
        List<Http2Header> extraHeaders = addHeaders.addHeaders(addr);

        return clientHelper.stream((ResponseStreamHandle)args[0], endpoint, extraHeaders);

    }

}
