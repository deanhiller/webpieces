package org.webpieces.microsvc.client.impl;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.microsvc.api.HttpMethod;
import org.webpieces.microsvc.api.Path;
import org.webpieces.util.context.ClientAssertions;
import org.webpieces.util.context.Context;

import javax.inject.Inject;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class HttpsJsonClientInvokeHandler implements InvocationHandler {

    private final Logger log = LoggerFactory.getLogger(HttpsJsonClientInvokeHandler.class);
    private final HttpsJsonClient clientHelper;
    private ClientAssertions clientAssertions;
    private InetSocketAddress addr;

    @Inject
    public HttpsJsonClientInvokeHandler(HttpsJsonClient clientHelper, ClientAssertions clientAssertions) {
        this.clientHelper = clientHelper;
        this.clientAssertions = clientAssertions;
    }

    public void setTargetAddress(InetSocketAddress addr) {
        this.addr = addr;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        clientAssertions.throwIfCannotGoRemote();

        if(args.length != 1) {
            throw new IllegalArgumentException("ALL clients should have EXACTLY 1 argument.  no more, no less.  Read GRPC as to why they do this too!!!");
        }

        Path annotation = method.getAnnotation(Path.class);

        if(annotation == null) {
            throw new IllegalArgumentException("The @Path annotation is missing from method=" + method+" clazz="+method.getDeclaringClass());
        }

        String path = annotation.value();
        HttpMethod httpMethod = annotation.method();
        Class<?> clazz = method.getReturnType();

        if(!(CompletableFuture.class.isAssignableFrom(clazz))) {
            throw new IllegalStateException("All api methods must return a CompletableFuture");
        }

        ParameterizedType t = (ParameterizedType)method.getGenericReturnType();
        Class retType = (Class)t.getActualTypeArguments()[0];

        Map<String, Object> context = Context.getContext();

        if(context == null) {
            throw new IllegalStateException("Please call Current.setContext with the request context or create your own context");
        }

        Object header = context.get(Context.HEADERS);
        if(header == null) {
            throw new IllegalStateException("Context.HEADERS is missing from the Context.  please set that up first");
        } else if(! (header instanceof Map)) {
            throw new IllegalStateException("Context.HEADERS is not a Map<String, String> and is setup incorrectly");
        }

        log.info("Sending http request to: " + addr.getHostName() + path);

        Endpoint endpoint = new Endpoint(addr, httpMethod.getMethod(), path);

        return clientHelper.sendHttpRequest(method, args[0], endpoint, retType)
            .thenApply(retVal -> {
                Context.restoreContext(context);
                return retVal;
            });

    }

}
