package com.buffalosw.executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executor;

public class NotificationRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(NotificationRunnable.class);
    private final Runnable runnable;
    private final Executor singleThreadExecutor;
    private final SerializedSessionExecutor serializedExecutor;

    public NotificationRunnable(Runnable runnable, Executor singleThreadExecutor, SerializedSessionExecutor serializedExecutor) {
        this.runnable = runnable;
        this.singleThreadExecutor = singleThreadExecutor;
        this.serializedExecutor = serializedExecutor;
    }

    @Override
    public void run() {
        try {
            runnable.run();
        } catch(Throwable e) {
            logger.error("Exception running runnable="+runnable, e);
        } finally {
            serializedExecutor.freeExecutorAndSession(singleThreadExecutor, runnable);
        }
    }
}
