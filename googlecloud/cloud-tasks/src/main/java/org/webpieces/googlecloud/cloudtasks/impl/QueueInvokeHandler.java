package org.webpieces.googlecloud.cloudtasks.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.googlecloud.cloudtasks.api.RemoteInvoker;
import org.webpieces.microsvc.impl.EndpointInfo;
import org.webpieces.microsvc.impl.TestCaseRecorder;
import org.webpieces.util.context.ClientAssertions;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import javax.ws.rs.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.net.InetSocketAddress;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Pattern;

import static org.webpieces.microsvc.impl.TestCaseRecorder.RECORDER_KEY;

public class QueueInvokeHandler implements InvocationHandler {
    private static final Pattern REGEX_SLASH_MERGE = Pattern.compile("/{2,}", Pattern.CASE_INSENSITIVE);

    private final Logger log = LoggerFactory.getLogger(QueueInvokeHandler.class);
    private InetSocketAddress addr;
    private RemoteInvoker remoteInvoker;
    private ClientAssertions clientAssertions;

    @Inject
    public QueueInvokeHandler(RemoteInvoker remoteInvoker, ClientAssertions clientAssertions) {
        this.remoteInvoker = remoteInvoker;
        this.clientAssertions = clientAssertions;
    }

    public void initialize(InetSocketAddress addr) {
        this.addr = addr;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

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

        if(!(XFuture.class.isAssignableFrom(clazz))) {
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
        XFuture<Void> xFuture = remoteInvoker.invoke(path, httpMethod, body)
//        Endpoint endpoint = new Endpoint(addr, httpMethod.getCode(), path);
//        XFuture<Object> xFuture = clientHelper.sendHttpRequest(method, body, endpoint, retType)
                .thenApply(retVal -> {
                    //Only needed by APIs/methods that return CompletableFuture :( not XFuture
                    Context.restoreContext(context);
                    return retVal;
                });

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
            recordingInfo.addFailure(exc1);
            return XFuture.failedFuture(exc1);
        }

        recordingInfo.addSuccessResponse(resp);
        return XFuture.completedFuture(resp);
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
