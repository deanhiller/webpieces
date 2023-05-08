package org.webpieces.router.api;

import java.lang.reflect.Method;

public class RecordingInfo {

    public static final String JSON_ENDPOINT_RESULT = "xwebpieces-json-endpoint-result";

    private Method method;
    private Object[] args;
    private Throwable failureResponse;
    private Object response;

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object[] getArgs() {
        return args;
    }

    public void setArgs(Object[] args) {
        this.args = args;
    }

    public Throwable getFailureResponse() {
        return failureResponse;
    }

    public void setFailureResponse(Throwable failureResponse) {
        this.failureResponse = failureResponse;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }
}
