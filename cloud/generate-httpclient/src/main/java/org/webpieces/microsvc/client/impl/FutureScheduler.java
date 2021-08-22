package org.webpieces.microsvc.client.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.exceptions.GatewayTimeoutException;
import org.webpieces.router.api.exceptions.TooManyRequestsException;

import javax.inject.Inject;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class FutureScheduler {

    private static final Logger log = LoggerFactory.getLogger(FutureScheduler.class);
    private ScheduledExecutorService svc;
    private Random random = new Random();

    @Inject
    public FutureScheduler(ScheduledExecutorService svc) {
        this.svc = svc;
    }

    public <T> CompletableFuture<T> runWithRetries(Supplier<CompletableFuture<T>> function, int numTries) {
        log.info("running. tries left="+numTries);

        CompletableFuture<T> future = function.get();

        int triesLeft = numTries - 1;
        Supplier<CompletableFuture<T>> nextTry = () -> runWithRetries(function, triesLeft);

        CompletableFuture<T> future2 = future.handle((resp, t) -> {
            if (t == null)
                return CompletableFuture.completedFuture(resp);
            else if(t instanceof GatewayTimeoutException) {
                if(triesLeft == 0) {
                    CompletableFuture<T> f = new CompletableFuture<>();
                    f.completeExceptionally(t);
                    return f;
                }
                return nextTry.get();
            } else if(t instanceof TooManyRequestsException) {
                if(triesLeft == 0) {
                    CompletableFuture<T> f = new CompletableFuture<>();
                    f.completeExceptionally(t);
                    return f;
                }
                int initialDelay = 3000;
                int delay = initialDelay + random.nextInt(8000);
                return schedule(nextTry, delay, TimeUnit.MILLISECONDS);
            }

            CompletableFuture<T> excFut = new CompletableFuture<>();
            excFut.completeExceptionally(t);
            return excFut;
        }).thenCompose(Function.identity());

        return future2;
    }

    private static class MyRunnable<T> implements Runnable {
        private final Supplier<CompletableFuture<T>> callable;
        private final CompletableFuture<T> future;

        public MyRunnable(Supplier<CompletableFuture<T>> callable, CompletableFuture<T> future) {
            this.callable = callable;
            this.future = future;
        }

        @Override
        public void run() {
            try {
                CompletableFuture<T> fut = callable.get();
                fut.handle( (resp, t) -> {
                    if(t != null) {
                        future.completeExceptionally(t);
                    } else {
                        future.complete(resp);
                    }
                    return null;
                });
            } catch (Throwable e) {
                future.completeExceptionally(e);
            }
        }
    }
    public <T> CompletableFuture<T> schedule(Supplier<CompletableFuture<T>> callable, long delay, TimeUnit unit) {
        CompletableFuture<T> future = new CompletableFuture<>();
        Runnable r = new MyRunnable<>(callable, future);

        svc.schedule(r, delay, unit);

        return future;
    }
}
