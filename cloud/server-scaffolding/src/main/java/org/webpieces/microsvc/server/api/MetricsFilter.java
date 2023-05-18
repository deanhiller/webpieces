package org.webpieces.microsvc.server.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.metrics.Monitoring;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

public class MetricsFilter extends RouteFilter<Void> {

    private static final Logger log = LoggerFactory.getLogger(MetricsFilter.class);

    private Monitoring monitoring;
    private IgnoreExceptions exceptionCheck;

    @Inject
    public MetricsFilter(Monitoring monitoring, IgnoreExceptions exceptionCheck) {
        this.monitoring = monitoring;
        this.exceptionCheck = exceptionCheck;
    }

    @Override
    public void initialize(Void initialConfig) {

    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        log.info("method call coming in="+meta.getLoadedController().getControllerMethod().getName());

        Method method = meta.getLoadedController().getControllerMethod();
        long start = System.currentTimeMillis();

        return nextFilter.invoke(meta)
                .handle((resp, e) -> recordMetrics(resp, e, method, meta.getCtx().getRequest().method, start))
                .thenCompose(Function.identity());
    }

    private XFuture<Action> recordMetrics(Action resp, Throwable e, Method method, HttpMethod httpMethod, long start) {

        Map<String, String> tags = Map.of(
                ServerMetrics.ServerMetricTags.CONTROLLER, method.getDeclaringClass().getSimpleName(),
                ServerMetrics.ServerMetricTags.METHOD, method.getName(),
                ServerMetrics.ServerMetricTags.HTTP_METHOD, httpMethod.name()
        );
        monitoring.endTimer( ServerMetrics.SERVER_REQUEST_TIME, tags, start);

        monitoring.incrementMetric(ServerMetrics.SERVER_REQUEST_COUNT, tags);

        if(e != null) {
            if(exceptionCheck.exceptionIsSuccess(e)) {
                recordSuccess(tags);
            } else {
                Map<String, String> errorTags = Map.of(
                        ServerMetrics.ServerExceptionMetricTags.CONTROLLER, method.getDeclaringClass().getSimpleName(),
                        ServerMetrics.ServerExceptionMetricTags.METHOD, method.getName(),
                        ServerMetrics.ServerMetricTags.HTTP_METHOD, httpMethod.name(),
                        ServerMetrics.ServerExceptionMetricTags.EXCEPTION, exceptionCheck.unwrapEx(e).getClass().getSimpleName()
                );
                monitoring.incrementMetric(ServerMetrics.SERVER_REQUEST_FAILURE, errorTags);
            }

            return XFuture.failedFuture(e);
        }

        recordSuccess(tags);

        return XFuture.completedFuture(resp);
    }

    private void recordSuccess(Map<String, String> tags) {
        monitoring.incrementMetric(ServerMetrics.SERVER_REQUEST_SUCCESS, tags);

    }

}
