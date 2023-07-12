package org.webpieces.util.threading;

import org.webpieces.metrics.Monitoring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @deprecated Use MetricsExecutors instead
 */
@Deprecated
public class FutureExecutors {

    /**
     * @deprecated Use MetricsExecutor.create instead
     */
    @Deprecated
    public FutureExecutor create(Monitoring monitoring, int threadPoolSize, String name) {
        return create(monitoring, threadPoolSize, name, false);
    }

    /**
     * @deprecated Use MetricsExecutor.create instead
     */
    @Deprecated
    public FutureExecutor createDirect() {
        return new DirectFutureExecutor();
    }

    /**
     * @deprecated Use MetricsExecutor.create instead
     */
    @Deprecated
    public FutureExecutor create(Monitoring monitoring, int threadPoolSize, String name, boolean isXFutureMDCAdapterInstalled) {
        NamedThreadFactory nFactory = new NamedThreadFactory(name);
        ScheduledExecutorService svc = Executors.newScheduledThreadPool(threadPoolSize, nFactory);
        return create(monitoring, svc, name, isXFutureMDCAdapterInstalled);
    }

    /**
     * @deprecated Use MetricsExecutor.create instead
     */
    @Deprecated
    public FutureExecutor create(Monitoring monitoring, ScheduledExecutorService svc, String name, boolean isXFutureMDCAdapterInstalled) {
        return new FutureExecutorImpl(monitoring, svc, name, isXFutureMDCAdapterInstalled);
    }

}
