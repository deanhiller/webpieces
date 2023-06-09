package org.webpieces.util.threading;

import org.webpieces.metrics.Monitoring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class FutureExecutors {

    public FutureExecutor create(Monitoring monitoring, int threadPoolSize) {
        ScheduledExecutorService svc = Executors.newScheduledThreadPool(threadPoolSize);
        return new FutureExecutorImpl(monitoring, svc);
    }
}
