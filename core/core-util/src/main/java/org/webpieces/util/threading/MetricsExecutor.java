package org.webpieces.util.threading;

import org.webpieces.util.futures.XFuture;

import java.util.Map;
import java.util.function.Supplier;

public interface MetricsExecutor {
    
    XFuture<Void> executeRunnable(Runnable function, Map<String, String> extraTags);

    <RESP> XFuture<RESP> execute(Supplier<RESP> function, Map<String, String> extraTags);

}
