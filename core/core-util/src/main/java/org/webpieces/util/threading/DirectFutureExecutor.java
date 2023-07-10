package org.webpieces.util.threading;

import org.webpieces.util.futures.XFuture;

import java.util.Map;
import java.util.function.Supplier;

public class DirectFutureExecutor implements FutureExecutor {
    @Override
    public XFuture<Void> executeRunnable(Runnable function, Map<String, String> extraTags) {
        try {
            function.run();
            return XFuture.completedFuture(null);
        } catch (Throwable e) {
            return XFuture.failedFuture(e);
        }
    }

    @Override
    public <RESP> XFuture<RESP> execute(Supplier<RESP> function, Map<String, String> extraTags) {
        try {
            RESP resp = function.get();
            return XFuture.completedFuture(resp);
        } catch (Throwable e) {
            return XFuture.failedFuture(e);
        }
    }
}
