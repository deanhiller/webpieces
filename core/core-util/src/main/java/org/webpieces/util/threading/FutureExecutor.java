package org.webpieces.util.threading;

import org.webpieces.util.futures.XFuture;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public interface FutureExecutor {


    <RESP> XFuture<RESP> execute(Supplier<RESP> function);

    <T> ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                               long initialDelay,
                                               long period,
                                               TimeUnit unit);
}
