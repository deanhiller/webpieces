package org.webpieces.microsvc.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.recorder.impl.EndpointInfo;
import org.webpieces.recorder.impl.TestCaseRecorder;
import org.webpieces.util.context.ClientAssertions;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.webpieces.recorder.impl.TestCaseRecorder.RECORDER_KEY;

public class HttpsJsonClientInvokeHandler implements InvocationHandler {

    private static final Pattern REGEX_SLASH_MERGE = Pattern.compile("/{2,}", Pattern.CASE_INSENSITIVE);

    private final Logger log = LoggerFactory.getLogger(HttpsJsonClientInvokeHandler.class);
    private final HttpsJsonClient clientHelper;
    private ClientAssertions clientAssertions;
    private InetSocketAddress addr;
    private boolean hasUrlParams;
    private boolean forHttp;

    @Inject
    public HttpsJsonClientInvokeHandler(HttpsJsonClient clientHelper, ClientAssertions clientAssertions) {
        this.clientHelper = clientHelper;
        this.clientAssertions = clientAssertions;
    }

    public void initialize(InetSocketAddress addr, boolean hasUrlParams, boolean forHttp) {
        this.addr = addr;
        this.hasUrlParams = hasUrlParams;
        this.forHttp = forHttp;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if(method.getDeclaringClass() == Object.class) {
            return method.invoke(this);
        }

        TestCaseRecorder recorder = (TestCaseRecorder) Context.get(RECORDER_KEY);
        EndpointInfo recordingInfo = null;
        if(recorder != null) {
            Map<String, Object> ctxSnapshot = Context.copyContext();
            recordingInfo = new EndpointInfo(method, args, ctxSnapshot);
            recorder.addEndpointInfo(recordingInfo);
        }

        clientAssertions.throwIfCannotGoRemote();

        Path annotation = method.getAnnotation(Path.class);

        if(annotation == null) {
            throw new IllegalArgumentException("The @Path annotation is missing from method=" + method+" clazz="+method.getDeclaringClass());
        }

        String path = getPath(method);
        HttpMethod httpMethod = getHttpMethod(method);
        Class<?> clazz = method.getReturnType();

        if(!(CompletableFuture.class.isAssignableFrom(clazz))) {
            throw new IllegalStateException("All api methods must return a XFuture");
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
        if(hasUrlParams) {
            path = transformPath(path, method, args);
            body = findBody(method, args);
        }

        Endpoint endpoint = new Endpoint(addr, httpMethod.getCode(), path);
        XFuture<Object> xFuture = clientHelper.sendHttpRequest(method, body, endpoint, retType, forHttp);

        if(recorder == null) {
            return xFuture;
        }

        EndpointInfo finalInfo = recordingInfo;
        return xFuture
                .handle( (resp, exc1) -> addTestRecordingInfo(finalInfo, resp, exc1))
                .thenCompose(Function.identity());
    }

    private XFuture<Object> addTestRecordingInfo(EndpointInfo recordingInfo, Object resp, Throwable exc1) {
        if(exc1 != null) {
            recordingInfo.setXfutureFailedResponse(exc1);
            return XFuture.failedFuture(exc1);
        }

        recordingInfo.setSuccessResponse(resp);
        return XFuture.completedFuture(resp);
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
