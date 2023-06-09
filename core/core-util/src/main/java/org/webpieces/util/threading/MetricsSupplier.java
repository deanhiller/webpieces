package org.webpieces.util.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.metrics.Monitoring;
import org.webpieces.util.futures.XFuture;

import java.util.Map;
import java.util.function.Supplier;

public class MetricsSupplier<RESP> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MetricsSupplier.class);

    private final Monitoring monitoring;
    private final Supplier<RESP> function;
    private final XFuture<RESP> future;
    private Map<String, String> tags;

    public MetricsSupplier(Monitoring monitoring, Supplier<RESP> function, XFuture<RESP> future, Map<String, String> tags) {
        this.monitoring = monitoring;
        this.function = function;
        this.future = future;
        this.tags = tags;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            monitoring.incrementMetric("event.count", tags);

            RESP resp = function.get();

            monitoring.incrementMetric("event.success", tags);
            completeSafely(resp);

        } catch (Throwable e) {
            tags.put("exception-type", e.getClass().getSimpleName());
            monitoring.incrementMetric("event.exception", tags);
            log.error("Exception", e);
            completeExceptionSafely(e);
        } finally {
            monitoring.endTimer("event.time", tags, startTime);
        }
    }

    private void completeExceptionSafely(Throwable e) {
        try {
            future.completeExceptionally(e);
        } catch (Throwable exc) {
            log.error("Exception completing with exception response", exc);
        }
    }

    private void completeSafely(RESP resp) {
        try {
            future.complete(resp);
        } catch (Throwable e) {
            log.error("Exception completing future", e);
        }
    }
}
