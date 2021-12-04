package org.webpieces.microsvc.client.impl;

import org.webpieces.microsvc.impl.EndpointInfo;
import org.webpieces.microsvc.impl.TestCaseRecorder;
import org.webpieces.util.context.Context;
import org.webpieces.util.exceptions.SneakyThrow;
import org.webpieces.util.futures.XFuture;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Map;

import static org.webpieces.microsvc.impl.TestCaseRecorder.RECORDER_KEY;

public class APIRecorderInvokeHandler implements InvocationHandler {
    private Object apiImplementation;

    public void init(Object apiImplementation) {
        this.apiImplementation = apiImplementation;
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

        try {
            Object invoke = method.invoke(apiImplementation);

            addTestRecordingInfo(recordingInfo, invoke, null);

            //kept simple and not dealing with CompletableFuture for now
            return invoke;
        } catch (Exception e) {
            addTestRecordingInfo(recordingInfo, null, e);
            throw SneakyThrow.sneak(e);
        }
    }

    private void addTestRecordingInfo(EndpointInfo recordingInfo, Object resp, Throwable exc1) {
        if(exc1 != null) {
            recordingInfo.addFailure(exc1);
            return;
        }

        recordingInfo.addSuccessResponse(resp);
    }
}
