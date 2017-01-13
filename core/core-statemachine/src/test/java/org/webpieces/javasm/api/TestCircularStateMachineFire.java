package org.webpieces.javasm.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import junit.framework.TestCase;

public class TestCircularStateMachineFire extends TestCase {
	
	private MockActionListener onList = new MockActionListener();
	private StateMachine sm;
	private String flipOn;
	private String flipOff;
	private State on;
	private Memento memento;
	private State off;
	private CompletableFuture<State> secondFuture;

	/**
	 * Creates an instance of TestStateMachine.
	 * 
	 * @param arg0
	 */
	public TestCircularStateMachineFire(String arg0) {
		super(arg0);
	}

	/**
	 * @see junit.framework.TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception {
		super.setUp();

		StateMachineFactory factory = StateMachineFactory.createFactory();
		sm = factory.createStateMachine("TestFailures");

		flipOn = "flipOn";
		flipOff = "flipOff";

		on = sm.createState("on");
		off = sm.createState("off");

		Transition onToOff = sm.createTransition(on, off, flipOff);
		Transition offToOn = sm.createTransition(off, on, flipOn);

		onToOff.addActionListener(new FireIntoStateMachine());
		offToOn.addActionListener((ActionListener) onList);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	@Test
	public void testBasic() throws InterruptedException, ExecutionException {
		memento = sm.createMementoFromState("id", on);

		CompletableFuture<State> future = sm.fireEvent(memento, flipOff);
		State state = future.get();
		Assert.assertEquals(off, state);
		
		//even though there is a circular fire, it just queues it up and works
		Assert.assertEquals(on, secondFuture.get());
	}

	private class FireIntoStateMachine implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			secondFuture = sm.fireEvent(memento, flipOn);
		}
	}
}
