package org.webpieces.util.threading;

import org.webpieces.metrics.Monitoring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class FutureExecutors {

    /**
     * @deprecated
     */
    @Deprecated
    public FutureExecutor create(Monitoring monitoring, int threadPoolSize, String name) {
        return create(monitoring, threadPoolSize, name, false);
    }

    /**
     * Typically used at test time to make a test single threaded OR use MockFutureExecutor which
     * simply caches it all for the test to retrieve it
     */
    public FutureExecutor createDirect() {
        return new DirectFutureExecutor();
    }

    public FutureExecutor create(Monitoring monitoring, int threadPoolSize, String name, boolean isXFutureMDCAdapterInstalled) {
        NamedThreadFactory nFactory = new NamedThreadFactory(name);
        ScheduledExecutorService svc = Executors.newScheduledThreadPool(threadPoolSize, nFactory);
        return create(monitoring, svc, name, isXFutureMDCAdapterInstalled);
    }

    public FutureExecutor create(Monitoring monitoring, ScheduledExecutorService svc, String name, boolean isXFutureMDCAdapterInstalled) {
        return new FutureExecutorImpl(monitoring, svc, name, isXFutureMDCAdapterInstalled);
    }

    public MetricsScheduleExecutor createScheduledExecutor(Monitoring monitoring, ScheduledExecutorService svc, String name, boolean isXFutureMDCAdapterInstalled) {
        return new MetricsScheduleExecutorImpl(monitoring, svc, name, isXFutureMDCAdapterInstalled);
    }
}
