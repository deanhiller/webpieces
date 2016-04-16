package com.buffalosw.executors;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class TestSessionExec {

    @Test
    public void testSameSession() {
        StubExecutor exec1 = new StubExecutor();
        StubExecutor exec2 = new StubExecutor();

        List<Executor> executors = new ArrayList<>();
        executors.add(exec1);
        executors.add(exec2);

        SerializedSessionExecutor executor = new SerializedSessionExecutor(executors);

        TestRunnable r = new TestRunnable("5");
        TestRunnable r2 = new TestRunnable("5");

        executor.execute(r);
        executor.execute(r2);

        simulateRunnable(exec1);

        Assert.assertTrue(r.isWasRun());
        Assert.assertFalse(r2.isWasRun());

        simulateRunnable(exec1);

        Assert.assertTrue(r2.isWasRun());
    }

    @Test
    public void testDifferentSessions() {
        StubExecutor exec1 = new StubExecutor();
        StubExecutor exec2 = new StubExecutor();

        List<Executor> executors = new ArrayList<>();
        executors.add(exec1);
        executors.add(exec2);

        SerializedSessionExecutor executor = new SerializedSessionExecutor(executors);

        TestRunnable r = new TestRunnable("5");
        TestRunnable r2 = new TestRunnable("4");

        executor.execute(r);
        executor.execute(r2);

        simulateRunnable(exec1);
        simulateRunnable(exec2);

        Assert.assertTrue(r.isWasRun());
        Assert.assertTrue(r2.isWasRun());
    }

    @Test
    public void testMix() {
        StubExecutor exec1 = new StubExecutor();
        StubExecutor exec2 = new StubExecutor();

        List<Executor> executors = new ArrayList<>();
        executors.add(exec1);
        executors.add(exec2);

        SerializedSessionExecutor executor = new SerializedSessionExecutor(executors);

        TestRunnable r = new TestRunnable("5");
        TestRunnable r2 = new TestRunnable("4");
        TestRunnable r3 = new TestRunnable("5");

        executor.execute(r);
        executor.execute(r2);
        executor.execute(r3);

        simulateRunnable(exec1);
        simulateRunnable(exec2);

        Assert.assertTrue(r.isWasRun());
        Assert.assertTrue(r2.isWasRun());
        Assert.assertFalse(r3.isWasRun());

        simulateRunnable(exec1);

        Assert.assertTrue(r2.isWasRun());
    }

    private void simulateRunnable(StubExecutor exec1) {
        List<Runnable> runnables = exec1.getRunnables();
        Assert.assertEquals(1, runnables.size());
        runnables.get(0).run();
    }
}
