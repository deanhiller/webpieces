package org.webpieces.javasm.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import junit.framework.TestCase;

/**
 */
public class TestFailures extends TestCase {
	
	private static final Logger log = Logger.getLogger(TestFailures.class.getName());
	private MockActionListener onList = new MockActionListener();
	private StateMachine sm;
	private String flipOn;
	private String flipOff;
	private State on;
	private Memento memento;
	

	/**
	 * Creates an instance of TestStateMachine.
	 * 
	 * @param arg0
	 */
	public TestFailures(String arg0) {
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
		State off = sm.createState("off");

		Transition onToOff = sm.createTransition(on, off, flipOff);
		Transition offToOn = sm.createTransition(off, on, flipOn);

		onToOff.addActionListener(new FireIntoStateMachine());
		offToOn.addActionListener((ActionListener) onList);
	}

	/**
	 * @see junit.framework.TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	public void testBasic() {
		memento = sm.createMementoFromState("id", on);

		try {
			sm.fireEvent(memento, flipOff);
			fail("Should have thrown exception....circular firing events into sm can't be allowed");
		} catch (IllegalFireEventException e) {
			log.info("This exception is expected");
		}
	}

	private class FireIntoStateMachine implements ActionListener {
		/**
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			sm.fireEvent(memento, flipOn);
		}
	}
}
