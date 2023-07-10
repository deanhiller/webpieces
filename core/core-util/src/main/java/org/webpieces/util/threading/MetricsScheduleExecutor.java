package org.webpieces.util.threading;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public interface MetricsScheduleExecutor {

    <T> ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                               long initialDelay,
                                               long period,
                                               TimeUnit unit,
                                               Map<String, String> extraMetricTags
    );

    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit,
                                                     Map<String, String> extraMetricTags
    );
}
