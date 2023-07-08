package org.webpieces.util.threading;

import org.webpieces.metrics.Monitoring;
import org.webpieces.util.futures.XFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Supplier;

public class FutureExecutorImpl implements FutureExecutor {
    private final String name;
    private Monitoring monitoring;
    private ScheduledExecutorService svc;

    public FutureExecutorImpl(Monitoring monitoring, ScheduledExecutorService svc, String name) {
        this.monitoring = monitoring;
        this.svc = svc;
        this.name = name;

        if(svc instanceof ThreadPoolExecutor) {
            ThreadPoolExecutor exec = (ThreadPoolExecutor) svc;
            BlockingQueue<Runnable> queue = exec.getQueue();
            Map<String, String> tag = Map.of("executorName", name);
            //every minute, metrics will call queue.size ->
            monitoring.gauge("event.queue.size", tag, queue, (q) -> queue.size());
        }
    }

    @Override
    public XFuture<Void> executeRunnable(Runnable function, Map<String, String> extraTags) {
        Map<String, String> tags = formTags(function.getClass(), extraTags);
        XFuture<Void> future = new XFuture<>();

        Supplier<Void> supplier = () -> {
            function.run();
            return null;
        };
        MetricsSupplier runnable = new MetricsSupplier(monitoring, supplier, future, tags);
        monitoring.incrementMetric("event.queued", tags);
        svc.execute(runnable);
        return future;
    }

    @Override
    public <RESP> XFuture<RESP> execute(Supplier<RESP> function, Map<String, String> extraTags) {
        Map<String, String> tags = formTags(function.getClass(), extraTags);
        XFuture<RESP> future = new XFuture<>();

        MetricsSupplier runnable = new MetricsSupplier(monitoring, function, future, tags);
        monitoring.incrementMetric("event.queued", tags);
        svc.execute(runnable);
        return future;
    }

    private <RESP> Map<String, String> formTags(Class<?> clazz, Map<String, String> extraTags) {
        Map<String, String> tags = new HashMap<>();
        tags.put("executorName", name);
        tags.put("type", clazz.getSimpleName());
        if(extraTags != null) {
            tags.putAll(extraTags);
        }
        return tags;
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

}
