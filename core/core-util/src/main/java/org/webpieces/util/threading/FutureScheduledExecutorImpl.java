package org.webpieces.util.threading;

import org.webpieces.metrics.Monitoring;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class FutureScheduledExecutorImpl implements FutureScheduledExecutor {
    private final Monitoring monitoring;
    private final ScheduledExecutorService svc;
    private final String name;

    public FutureScheduledExecutorImpl(Monitoring monitoring, ScheduledExecutorService svc, String name) {
        this.monitoring = monitoring;
        this.svc = svc;
        this.name = name;
    }

    @Override
    public <T> ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                      long initialDelay,
                                                      long period,
                                                      TimeUnit unit,
                                                      Map<String, String> extraTags) {
        Map<String, String> tags = formTags(command.getClass(), extraTags);
        MetricsRunnable r = new MetricsRunnable<Void>(monitoring, command, tags);
        return svc.scheduleAtFixedRate(r, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit, Map<String, String> extraMetricTags) {
        Map<String, String> tags = formTags(command.getClass(), extraMetricTags);
        MetricsRunnable r = new MetricsRunnable<Void>(monitoring, command, tags);
        return svc.scheduleAtFixedRate(r, initialDelay, delay, unit);
    }

    private Map<String, String> formTags(Class<?> clazz, Map<String, String> extraTags) {
        Map<String, String> tags = new HashMap<>();
        tags.put("executorName", name);
        tags.put("type", clazz.getSimpleName());
        if(extraTags != null) {
            tags.putAll(extraTags);
        }
        return tags;
    }
}
