package com.buffalosw.executors;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SerializedSessionExecutor implements Executor {

    private final int numThreads;
    private Set<String> currentlyRunningSessions = new HashSet<>();
    private List<Runnable> runnables = new ArrayList<>();
    private List<Executor> availableExecutors = new ArrayList<>();
    private List<Executor> runningExecutors = new ArrayList<>();

    public SerializedSessionExecutor(List<Executor> executors) {
        this.numThreads = executors.size();
        availableExecutors = executors;
    }

    public SerializedSessionExecutor(int numThreads) {
        this.numThreads = numThreads;
        for(int i = 0; i < numThreads; i++) {
            ExecutorService executor = Executors.newSingleThreadExecutor();
            availableExecutors.add(executor);
        }
    }

    @Override
    public synchronized void execute(Runnable runnable) {
        runnables.add(runnable);

        if(currentlyRunningSessions.size() >= numThreads)
            return; //all threads are busy right now

        moveRunnableToRunning(null);
    }

    public synchronized void freeExecutorAndSession(Executor executor, Runnable runnable) {
        if(runnable instanceof SessionRunnable) {
            String id = ((SessionRunnable)runnable).getSessionId();
            currentlyRunningSessions.remove(id);
        }

        if(runnables.size() == 0) {
            runningExecutors.remove(executor);
            availableExecutors.add(executor);
            return;
        }

        moveRunnableToRunning(executor);
    }

    synchronized void moveRunnableToRunning(Executor executorToUse) {
        List<Runnable> queue = runnables;
        for(int i = 0; i < queue.size(); i++) {
            Runnable runnable = queue.get(i);
            if(!(runnable instanceof SessionRunnable)) {
                runOnAvailableExecutor(executorToUse, runnable, null);
                queue.remove(runnable);
                return;
            }
            SessionRunnable sessionRunnable = (SessionRunnable)runnable;
            String sessionId = sessionRunnable.getSessionId();
            if(!currentlyRunningSessions.contains(sessionId)) {
                runOnAvailableExecutor(executorToUse, runnable, sessionId);
                queue.remove(runnable);
                return;
            }
        }
    }

    private void runOnAvailableExecutor(Executor justUsedExecutor, Runnable runnable, String sessionId) {
        Executor executorToUse = justUsedExecutor;
        if(executorToUse == null) {
            executorToUse = availableExecutors.remove(0);
            runningExecutors.add(executorToUse);
        }

        if(sessionId != null)
            currentlyRunningSessions.add(sessionId);

        NotificationRunnable r = new NotificationRunnable(runnable, executorToUse, this);
        executorToUse.execute(r);
    }

}
