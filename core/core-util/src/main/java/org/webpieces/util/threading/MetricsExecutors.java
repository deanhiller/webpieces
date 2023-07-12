package org.webpieces.util.threading;

import org.webpieces.metrics.Monitoring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class MetricsExecutors {

    /**
     * Typically used at test time to make a test single threaded OR use MockFutureExecutor which
     * simply caches it all for the test to retrieve it
     */
    public MetricsExecutor createDirect() {
        return new DirectMetricsExecutor();
    }

    public MetricsExecutor create(Monitoring monitoring, int threadPoolSize, String name, boolean isXFutureMDCAdapterInstalled) {
        NamedThreadFactory nFactory = new NamedThreadFactory(name);
        ScheduledExecutorService svc = Executors.newScheduledThreadPool(threadPoolSize, nFactory);
        return create(monitoring, svc, name, isXFutureMDCAdapterInstalled);
    }

    public MetricsExecutor create(Monitoring monitoring, ScheduledExecutorService svc, String name, boolean isXFutureMDCAdapterInstalled) {
        return new MetricsExecutorImpl(monitoring, svc, name, isXFutureMDCAdapterInstalled);
    }

    public MetricsScheduleExecutor createScheduledExecutor(Monitoring monitoring, ScheduledExecutorService svc, String name, boolean isXFutureMDCAdapterInstalled) {
        return new MetricsScheduleExecutorImpl(monitoring, svc, name, isXFutureMDCAdapterInstalled);
    }
}
