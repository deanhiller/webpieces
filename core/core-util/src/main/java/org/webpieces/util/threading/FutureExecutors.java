package org.webpieces.util.threading;

import org.webpieces.metrics.Monitoring;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class FutureExecutors {

    public FutureExecutor create(Monitoring monitoring, int threadPoolSize, String name) {
        ThreadFactory threadFactory = new ThreadFactory() {
            private AtomicInteger counter = new AtomicInteger(0);
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread();
                t.setDaemon(true); // a thread pool should never prevent jvm from exiting and should be daemon
                t.setName(name+counter.getAndAdd(1));
                return t;
            }
        };
        ScheduledExecutorService svc = Executors.newScheduledThreadPool(threadPoolSize, threadFactory);
        return create(monitoring, svc, name);
    }

    public FutureExecutor create(Monitoring monitoring, ScheduledExecutorService svc, String name) {
        return new FutureExecutorImpl(monitoring, svc, name);
    }
}
