package org.webpieces.googlecloud.cloudtasks.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.tasks.v2.CloudTasksClient;
import com.google.inject.Inject;
import org.webpieces.ctx.api.HttpMethod;

import javax.ws.rs.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.regex.Pattern;

public class GCPTasksClient implements InvocationHandler {

    private static final Pattern REGEX_SLASH_MERGE = Pattern.compile("/{2,}", Pattern.CASE_INSENSITIVE);

    private final CloudTasksClient cloudTaskClient;

    @Inject
    private ObjectMapper mapper;

    public GCPTasksClient(CloudTasksClient cloudTasksClient) {
        this.cloudTaskClient = cloudTasksClient;
    }
    
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        return null;
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
