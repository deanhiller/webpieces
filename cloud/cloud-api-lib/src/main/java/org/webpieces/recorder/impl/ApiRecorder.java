package org.webpieces.recorder.impl;

import jdk.management.jfr.RecordingInfo;
import org.webpieces.util.SneakyThrow;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import static org.webpieces.recorder.impl.TestCaseRecorder.RECORDER_KEY;

public class ApiRecorder implements InvocationHandler {
    private Object apiImplementation;

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        TestCaseRecorder recorder = Context.get(RECORDER_KEY);
        EndpointInfo recordingInfo = null;
        if(recorder != null) {
            Map<String, Object> ctxSnapshot = Context.copyContext();
            recordingInfo = new EndpointInfo(method, args, ctxSnapshot);
            recorder.addEndpointInfo(recordingInfo);
        }

        try {
            return invoke(method, recordingInfo, args);
        } catch (Throwable t) {
            recordingInfo.setFailureResponse(t);
            throw SneakyThrow.sneak(t);
        }
    }

    private Object invoke(Method method, EndpointInfo recordingInfo, Object[] args) throws IllegalAccessException, InvocationTargetException {
        Object result = method.invoke(apiImplementation, args);
        if (result instanceof XFuture) {
            XFuture<Object> xFuture = (XFuture<Object>) result;
            return xFuture
                    .handle((resp, e) -> record(resp, e, recordingInfo))
                    .thenCompose(Function.identity());
        } else if (result instanceof CompletableFuture) {
            CompletableFuture<Object> future = (CompletableFuture<Object>) result;
            XFuture<Object> xFuture = XFuture.completedFuture(null).thenCompose((voi) -> future);
            return xFuture
                    .handle((resp, e) -> record(resp, e, recordingInfo))
                    .thenCompose(Function.identity());
        }

        recordingInfo.setSuccessResponse(result);
        return result;
    }

    private XFuture<Object> record(Object resp, Throwable e, EndpointInfo recordingInfo) {
        if(e != null) {
            recordingInfo.setXfutureFailedResponse(e);
            return XFuture.failedFuture(e);
        }

        recordingInfo.setSuccessResponse(resp);
        return XFuture.completedFuture(resp);
    }

    public void init(Object apiImplementation) {
        this.apiImplementation = apiImplementation;
    }
}
