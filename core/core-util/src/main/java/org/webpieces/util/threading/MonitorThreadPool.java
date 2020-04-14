package org.webpieces.util.threading;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.binder.jvm.ExecutorServiceMetrics;

import java.util.concurrent.Executor;

public class MonitorThreadPool {
    public static void monitor(MeterRegistry metrics, Executor executor, String id) {
        Tag tag = Tag.of("name", id);
        ExecutorServiceMetrics.monitor(metrics, executor, "threadpool", tag);
    }
}
