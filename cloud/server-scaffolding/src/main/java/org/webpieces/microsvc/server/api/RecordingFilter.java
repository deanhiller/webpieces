package org.webpieces.microsvc.server.api;

import org.webpieces.ctx.api.Current;
import org.webpieces.ctx.api.RequestContext;
import org.webpieces.ctx.api.RouterHeader;
import org.webpieces.microsvc.api.MicroSvcHeader;
import org.webpieces.microsvc.impl.TestCaseRecorder;
import org.webpieces.microsvc.server.impl.TestCaseRecorderImpl;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import java.util.List;
import java.util.Map;

public class RecordingFilter extends RouteFilter<Void> {

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
        RequestContext context = Current.getContext();
        List<RouterHeader> routerHeaders = context.getRequest().getHeaders().get(MicroSvcHeader.RECORDING.getHeaderName());
        if(routerHeaders == null)
            return nextFilter.invoke(meta);

        Map<String, Object> fullRequestContext = Context.copyContext();
        //let the recording begin...
        Context.put(TestCaseRecorder.RECORDER_KEY, new TestCaseRecorderImpl(Current.getContext().getRequest().originalRequest, meta, fullRequestContext));
        return nextFilter.invoke(meta)
                .thenApply((resp) -> writeOutTestCase(resp));
    }

    private Action writeOutTestCase(Action resp) {
        TestCaseRecorderImpl recorder = (TestCaseRecorderImpl) Context.get(TestCaseRecorder.RECORDER_KEY);
        recorder.spitOutTestCase();
        return resp;
    }

    @Override
    public void initialize(Void initialConfig) {

    }
}
