package org.webpieces.microsvc.client.impl;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Pattern;
import javax.inject.Inject;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.microsvc.api.NotEvolutionProof;
import org.webpieces.util.context.ClientAssertions;
import org.webpieces.util.context.Context;
import org.webpieces.util.urlparse.RegExResult;
import org.webpieces.util.urlparse.RegExUtil;

public class HttpsJsonClientInvokeHandler implements InvocationHandler {

    private static final Pattern REGEX_SLASH_MERGE = Pattern.compile("/{2,}", Pattern.CASE_INSENSITIVE);

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

        Path annotation = method.getAnnotation(Path.class);

        if(annotation == null) {
            throw new IllegalArgumentException("The @Path annotation is missing from method=" + method+" clazz="+method.getDeclaringClass());
        }

        String path = getPath(method);
        HttpMethod httpMethod = getHttpMethod(method);
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

        log.info("Sending http request to: " + addr.getHostName()+":"+addr.getPort() + path);

        Object body = args[0];
        if(method.getAnnotation(NotEvolutionProof.class) != null) {
            path = transformPath(path, method, args);
            body = findBody(method, args);
        }

        Endpoint endpoint = new Endpoint(addr, httpMethod.getCode(), path);

        return clientHelper.sendHttpRequest(method, body, endpoint, retType)
            .thenApply(retVal -> {
                Context.restoreContext(context);
                return retVal;
            });

    }

    private String transformPath(String path, Method method, Object[] args) {
        String methodName = method.getName();
        String requestName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1)+"Request";

        Parameter[] parameters = method.getParameters();
        for(int i = 0; i < args.length; i++) {
            Parameter param = parameters[i];
            if(param.getType().getSimpleName().equals(requestName))
                continue; // skip the body

            String name = param.getName();
            String variable = "{"+name+"}";
            if(!path.contains(variable))
                throw new IllegalArgumentException("Can't find '"+variable+"' in the path to bind in the url");
            path = path.replace("{"+name+"}", args[i]+"");
        }

        return path;
    }

    private Object findBody(Method method, Object[] args) {
        String methodName = method.getName();
        String requestName = methodName.substring(0, 1).toUpperCase() + methodName.substring(1)+"Request";
        for(Object arg : args) {
            if(arg.getClass().getSimpleName().equals(requestName))
                return arg;
        }
        return null;
    }

    protected String getPath(Method method) {

        Path path = method.getAnnotation(Path.class);

        if(path == null) {
            throw new IllegalArgumentException("The @Path annotation is missing from method=" + method);
        }

        String pathValue = getFullPath(method);

        if((pathValue == null) || pathValue.isBlank()) {
            throw new IllegalStateException("Invalid value for @Path annotation on " + method.getName() + ": " + pathValue);
        }

        return pathValue;

    }

    private String getFullPath(Method method) {

        Path cPath = method.getDeclaringClass().getAnnotation(Path.class);
        Path mPath = method.getAnnotation(Path.class);

        String cPathValue = null;
        String mPathValue = null;

        if(cPath != null) {
            cPathValue = cPath.value();
        }

        if(mPath != null) {
            mPathValue = mPath.value();
        }

        StringBuilder sb = new StringBuilder();

        if(cPathValue != null) {
            sb.append(cPathValue);
        }

        if(mPathValue != null) {
            sb.append(mPathValue);
        }

        String path = REGEX_SLASH_MERGE.matcher(sb.toString().trim()).replaceAll("/");

        return path;

    }

    protected HttpMethod getHttpMethod(Method method) {

        Path path = method.getAnnotation(Path.class);

        if(path == null) {
            throw new IllegalArgumentException("The @Path annotation is missing from method=" + method);
        }

        HttpMethod httpMethod;

        if(method.getAnnotation(DELETE.class) != null) {
            httpMethod = HttpMethod.DELETE;
        }
        else if(method.getAnnotation(GET.class) != null) {
            httpMethod = HttpMethod.GET;
        }
        else if(method.getAnnotation(OPTIONS.class) != null) {
            httpMethod = HttpMethod.OPTIONS;
        }
        else if(method.getAnnotation(POST.class) != null) {
            httpMethod = HttpMethod.POST;
        }
        else if(method.getAnnotation(PUT.class) != null) {
            httpMethod = HttpMethod.PUT;
        }
        else {
            throw new IllegalStateException("Missing or unsupported HTTP method annotation on " + method.getName() + ": Must be @DELETE,@GET,@OPTIONS,@PATCH,@POST,@PUT");
        }

        return httpMethod;

    }

}
