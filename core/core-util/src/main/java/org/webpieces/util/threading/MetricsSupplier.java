package org.webpieces.util.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.metrics.Monitoring;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;
import org.webpieces.util.futures.XFutureMDCAdapter;

import java.util.Map;
import java.util.function.Supplier;

public class MetricsSupplier<RESP> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MetricsSupplier.class);

    private final Monitoring monitoring;
    private final Supplier<RESP> function;
    private final XFuture<RESP> future;
    private Map<String, String> tags;
    private boolean isXFutureMDCAdapterInstalled;
    private final Map<String, Object> context;
    private Map<String, String> loggingMdcMap;

    public MetricsSupplier(Monitoring monitoring, Supplier<RESP> function, XFuture<RESP> future, Map<String, String> tags, boolean isXFutureMDCAdapterInstalled) {
        this.monitoring = monitoring;
        this.function = function;
        this.future = future;
        this.tags = tags;
        this.isXFutureMDCAdapterInstalled = isXFutureMDCAdapterInstalled;
        context = Context.copyContext();

        if(!this.isXFutureMDCAdapterInstalled) {
            //hack mdc until we fix
            loggingMdcMap = MDC.getCopyOfContextMap();
        }
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        Map<String, Object> previously = Context.getContext();
        Map<String, String> previousLogMap = MDC.getCopyOfContextMap();
        Context.setContext(this.context);
        if(!isXFutureMDCAdapterInstalled)
            MDC.setContextMap(this.loggingMdcMap);

        try {
            monitoring.incrementMetric("event.started", tags);

            RESP resp = function.get();

            monitoring.incrementMetric("event.success", tags);
            completeSafely(resp);

        } catch (Throwable e) {
            tags.put("exception", e.getClass().getSimpleName());
            monitoring.incrementMetric("event.exception", tags);
            log.error("Exception", e);
            completeExceptionSafely(e);
        } finally {
            monitoring.endTimer("event.time", tags, startTime);
            Context.setContext(previously);
            if(!isXFutureMDCAdapterInstalled)
                MDC.setContextMap(previousLogMap);
        }
    }

    protected void completeExceptionSafely(Throwable e) {
        try {
            future.completeExceptionally(e);
        } catch (Throwable exc) {
            log.error("Exception completing with exception response", exc);
        }
    }

    protected void completeSafely(RESP resp) {
        try {
            future.complete(resp);
        } catch (Throwable e) {
            log.error("Exception completing future", e);
        }
    }
}
