package org.webpieces.util.threading;

import org.webpieces.metrics.Monitoring;
import org.webpieces.util.futures.XFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class MetricsScheduleExecutorImpl implements MetricsScheduleExecutor {
    private final String name;
    private boolean isXFutureMDCAdapterInstalled;
    private Monitoring monitoring;
    private ScheduledExecutorService svc;

    public MetricsScheduleExecutorImpl(Monitoring monitoring, ScheduledExecutorService svc, String name, boolean isXFutureMDCAdapterInstalled) {
        this.monitoring = monitoring;
        this.svc = svc;
        this.name = name;
        this.isXFutureMDCAdapterInstalled = isXFutureMDCAdapterInstalled;

        if(svc instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor exec = (ThreadPoolExecutor) svc;
            BlockingQueue<Runnable> queue = exec.getQueue();
            Map<String, String> tag = Map.of("executorName", name);
            //every minute, metrics will call queue.size ->
            monitoring.gauge("event.queue.size", tag, queue, (q) -> queue.size());
        }
    }

    @Override
    public <T> ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                      long initialDelay,
                                                      long period,
                                                      TimeUnit unit,
                                                      Map<String, String> extraTags) {
        Map<String, String> tags = formTags(command.getClass(), extraTags);
        Supplier<Void> supplier = () -> {
            command.run();
            return null;
        };
        MetricsRunnable r = new MetricsRunnable(monitoring, supplier, tags, isXFutureMDCAdapterInstalled);
        return svc.scheduleAtFixedRate(r, initialDelay, period, unit);
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit, Map<String, String> extraMetricTags) {
        Map<String, String> tags = formTags(command.getClass(), extraMetricTags);
        Supplier<Void> supplier = () -> {
            command.run();
            return null;
        };
        MetricsRunnable r = new MetricsRunnable(monitoring, supplier, tags, isXFutureMDCAdapterInstalled);
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
