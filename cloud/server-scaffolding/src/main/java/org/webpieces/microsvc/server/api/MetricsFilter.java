package org.webpieces.microsvc.server.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.function.Function;

public class MetricsFilter extends RouteFilter<Void> {

    private static final Logger log = LoggerFactory.getLogger(MetricsFilter.class);

    private IgnoreExceptions exceptionCheck;

    @Inject
    public MetricsFilter(IgnoreExceptions exceptionCheck) {
        this.exceptionCheck = exceptionCheck;
    }

    @Override
    public void initialize(Void initialConfig) {

    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        log.info("Implement metrics here");

        log.info("method call coming in="+meta.getLoadedController().getControllerMethod().getName());

        Method method = meta.getLoadedController().getControllerMethod();
        long start = System.currentTimeMillis();

        return nextFilter.invoke(meta)
                .handle((resp, e) -> recordMetrics(resp, e, method, meta.getCtx().getRequest().method, start))
                .thenCompose(Function.identity());
    }

    private XFuture<Action> recordMetrics(Action resp, Throwable e, Method method, HttpMethod httpMethod, long start) {
        if(e != null) {
            if(exceptionCheck.exceptionIsSuccess(e)) {
                recordSuccess();
            } else {
                recordFailure();
            }

            return XFuture.failedFuture(e);
        }

        recordSuccess();

        return XFuture.completedFuture(resp);
    }

    private void recordFailure() {

    }

    private void recordSuccess() {

    }
}
