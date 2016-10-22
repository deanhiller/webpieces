package org.webpieces.util.futures;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.util.threading.SessionExecutor;
import org.webpieces.util.threading.SessionExecutorImpl;

public class TestSessionExecutor {

	private ExecutorSimulator mockExec = new ExecutorSimulator();
	private SessionExecutor sessionExecutor = new SessionExecutorImpl(mockExec);
	
	@Test
	public void testBasic() {
		ComparableRunnable run1 = new ComparableRunnable();
		ComparableRunnable run2 = new ComparableRunnable();
		ComparableRunnable run3 = new ComparableRunnable();
		sessionExecutor.execute("a", run1);
		sessionExecutor.execute("a", run2);
		sessionExecutor.execute("b", run3);
		
		Assert.assertFalse(run1.wasRun);
		Assert.assertFalse(run2.wasRun);
		Assert.assertFalse(run3.wasRun);
		
		mockExec.runRunnables();
		
		Assert.assertTrue(run1.wasRun);
		Assert.assertFalse(run2.wasRun);
		Assert.assertTrue(run3.wasRun);
		
		mockExec.runRunnables();
		
		Assert.assertTrue(run2.wasRun);
		
		ComparableRunnable run4 = new ComparableRunnable();
		sessionExecutor.execute("a", run4);
		
		Assert.assertFalse(run4.wasRun);
		
		mockExec.runRunnables();
		
		Assert.assertTrue(run4.wasRun);
	}
	
	@Test
	public void testQueued() {
		ComparableRunnable run1 = new ComparableRunnable();
		ComparableRunnable run2 = new ComparableRunnable();
		ComparableRunnable run3 = new ComparableRunnable();
		sessionExecutor.execute("a", run1);
		sessionExecutor.execute("a", run2);
		sessionExecutor.execute("a", run3);
		
		Assert.assertFalse(run1.wasRun);
		Assert.assertFalse(run2.wasRun);
		Assert.assertFalse(run3.wasRun);
		
		mockExec.runRunnables();
		
		Assert.assertTrue(run1.wasRun);
		Assert.assertFalse(run2.wasRun);
		Assert.assertFalse(run3.wasRun);
		
		mockExec.runRunnables();
		
		Assert.assertTrue(run2.wasRun);
		Assert.assertFalse(run3.wasRun);
		
		mockExec.runRunnables();
		
		Assert.assertTrue(run3.wasRun);
	}
}
