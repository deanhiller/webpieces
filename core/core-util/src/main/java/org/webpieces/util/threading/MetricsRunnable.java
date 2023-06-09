package org.webpieces.util.threading;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.metrics.Monitoring;
import org.webpieces.util.futures.XFuture;

import java.util.Map;
import java.util.function.Supplier;

public class MetricsRunnable<RESP> implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(MetricsRunnable.class);

    private final Monitoring monitoring;
    private final Runnable function;
    private Map<String, String> tags;

    public MetricsRunnable(Monitoring monitoring, Runnable function, Map<String, String> tags) {
        this.monitoring = monitoring;
        this.function = function;
        this.tags = tags;
    }

    @Override
    public void run() {
        long startTime = System.currentTimeMillis();
        try {
            monitoring.incrementMetric("event.count", tags);

            function.run();

            monitoring.incrementMetric("event.success", tags);

        } catch (Throwable e) {
            tags.put("exception-type", e.getClass().getSimpleName());
            monitoring.incrementMetric("event.exception", tags);
            log.error("Exception", e);
        } finally {
            monitoring.endTimer("event.time", tags, startTime);
        }
    }

}
