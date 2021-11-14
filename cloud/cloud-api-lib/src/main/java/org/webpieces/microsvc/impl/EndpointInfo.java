package org.webpieces.microsvc.impl;

import java.lang.reflect.Method;
import java.util.Map;

public class EndpointInfo {
    private final Method method;
    private final Object[] args;
    private final Map<String, Object> ctxSnapshot;
    private Throwable failureResponse;
    private Object successResponse;

    public EndpointInfo(Method method, Object[] args, Map<String, Object> ctxSnapshot) {
        this.method = method;
        this.args = args;
        this.ctxSnapshot = ctxSnapshot;
    }

    public Method getMethod() {
        return method;
    }

    public Object[] getArgs() {
        return args;
    }

    public Map<String, Object> getCtxSnapshot() {
        return ctxSnapshot;
    }

    public Throwable getFailureResponse() {
        return failureResponse;
    }

    public void setFailureResponse(Throwable failureResponse) {
        this.failureResponse = failureResponse;
    }

    public Object getSuccessResponse() {
        return successResponse;
    }

    public void setSuccessResponse(Object successResponse) {
        this.successResponse = successResponse;
    }

    public void addFailure(Throwable failureResponse) {
        this.failureResponse = failureResponse;
    }

    public void addSuccessResponse(Object successResponse) {
        this.successResponse = successResponse;
    }
}
