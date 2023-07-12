package org.webpieces.util.threading;

import org.webpieces.util.futures.XFuture;

import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @deprecated Use MetricsExecutor instead
 */
@Deprecated
public interface FutureExecutor {
    /**
     * @deprecated Use MetricsExecutor.executeRunnable instead
     */
    @Deprecated
    XFuture<Void> executeRunnable(Runnable function, Map<String, String> extraTags);

    /**
     * @deprecated Use MetricsExecutor.execute instead
     */
    @Deprecated
    <RESP> XFuture<RESP> execute(Supplier<RESP> function, Map<String, String> extraTags);

}
