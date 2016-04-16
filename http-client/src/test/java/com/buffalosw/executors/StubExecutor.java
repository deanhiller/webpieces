package com.buffalosw.executors;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;


public class StubExecutor implements Executor {
    private List<Runnable> runnables = new ArrayList<>();

    @Override
    public void execute(Runnable command) {
        runnables.add(command);
    }

    public List<Runnable> getRunnables() {
        List<Runnable> queue = new ArrayList<>(runnables);
        runnables.clear();
        return queue;
    }
}
