package org.webpieces.util.threading;

import org.webpieces.metrics.Monitoring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class FutureExecutors {

    public FutureExecutor create(Monitoring monitoring, int threadPoolSize, String name) {
        NamedThreadFactory nFactory = new NamedThreadFactory(name);
        ScheduledExecutorService svc = Executors.newScheduledThreadPool(threadPoolSize, nFactory);
        return create(monitoring, svc, name);
    }

    public FutureExecutor create(Monitoring monitoring, ScheduledExecutorService svc, String name) {
        return new FutureExecutorImpl(monitoring, svc, name);
    }
}
