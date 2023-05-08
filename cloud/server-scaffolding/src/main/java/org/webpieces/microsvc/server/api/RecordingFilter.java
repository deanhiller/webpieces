package org.webpieces.microsvc.server.api;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.microsvc.api.MicroSvcHeader;
import org.webpieces.recorder.impl.EndpointInfo;
import org.webpieces.recorder.impl.TestCaseRecorder;
import org.webpieces.microsvc.server.impl.TestCaseRecorderImpl;
import org.webpieces.router.api.RecordingInfo;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import java.util.Map;

public class RecordingFilter extends RouteFilter<Void> {

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
        RequestContext context = Current.getContext();
        String magic = Context.getMagic(MicroSvcHeader.RECORDING);
        if(magic == null)
            return nextFilter.invoke(meta);

        Map<String, Object> fullRequestContext = Context.copyContext();
        //let the recording begin...
        Context.put(TestCaseRecorder.RECORDER_KEY, new TestCaseRecorderImpl(Current.getContext().getRequest().originalRequest, meta, fullRequestContext));
        Context.put(RecordingInfo.JSON_ENDPOINT_RESULT, new RecordingInfo());
        return nextFilter.invoke(meta)
                .thenApply((resp) -> writeOutTestCase(resp, fullRequestContext));
    }

    private Action writeOutTestCase(Action resp, Map<String, Object> fullRequestContext) {
        TestCaseRecorderImpl recorder = (TestCaseRecorderImpl) Context.get(TestCaseRecorder.RECORDER_KEY);
        RecordingInfo info = Context.get(RecordingInfo.JSON_ENDPOINT_RESULT);
        EndpointInfo microSvcEndpoint = new EndpointInfo(info.getMethod(), info.getArgs(), fullRequestContext);
        microSvcEndpoint.setSuccessResponse(info.getResponse());
        microSvcEndpoint.setFailureResponse(info.getFailureResponse());

        recorder.spitOutTestCase(microSvcEndpoint);
        return resp;
    }

    @Override
    public void initialize(Void initialConfig) {

    }
}
