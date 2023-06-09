package org.webpieces.util.threading;

import org.webpieces.metrics.Monitoring;
import org.webpieces.util.context.Context;
import org.webpieces.util.futures.XFuture;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class FutureExecutorImpl implements FutureExecutor {
    private Monitoring monitoring;
    private ScheduledExecutorService svc;

    public FutureExecutorImpl(Monitoring monitoring, ScheduledExecutorService svc) {
        this.monitoring = monitoring;
        this.svc = svc;
    }

    @Override
    public <RESP> XFuture<RESP> execute(Supplier<RESP> function) {
        Map<String, String> tags = formTags(function.getClass());
        XFuture<RESP> future = new XFuture<>();

        MetricsSupplier runnable = new MetricsSupplier(monitoring, function, future, tags);
        svc.execute(runnable);
        return future;
    }

    private <RESP> Map<String, String> formTags(Class<?> clazz) {
        Map<String, String> tags = new HashMap<>();
        tags.put("type", clazz.getSimpleName());
        Map<String, String> extraTags = (Map<String, String>) Context.getContext().get("metrics.dimensions");
        if(extraTags != null) {
            tags.putAll(extraTags);
        }
        return tags;
    }

    @Override
    public <T> ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                      long initialDelay,
                                                      long period,
                                                      TimeUnit unit) {
        Map<String, String> tags = formTags(command.getClass());
        MetricsRunnable r = new MetricsRunnable<Void>(monitoring, command, tags);
        return svc.scheduleAtFixedRate(r, initialDelay, period, unit);
    }


}
