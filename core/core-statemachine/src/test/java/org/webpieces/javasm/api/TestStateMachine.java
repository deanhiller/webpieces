package org.webpieces.javasm.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import junit.framework.TestCase;

/**
 */
public class TestStateMachine extends TestCase
{
	private static final Logger log = Logger.getLogger(TestStateMachine.class.getName());
    private MockActionListener mockOffListener = new MockActionListener();
    private MockActionListener mockOnListener = new MockActionListener();
    private StateMachine sm;
    private String flipOn;
    private String flipOff;
    private State on;
    private Transition onToOff;

    /**
     * Creates an instance of TestStateMachine.
     * @param arg0
     */
    public TestStateMachine(String arg0)
    {
        super(arg0);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        StateMachineFactory factory = StateMachineFactory.createFactory();
        sm = factory.createStateMachine("TestStateMachine");

        flipOn = "flipOn";
        flipOff = "flipOff";

        on = sm.createState("on");
        State off = sm.createState("off");

        onToOff = sm.createTransition(on, off, flipOff);
        onToOff.addActionListener(mockOffListener);

        Transition offToOn = sm.createTransition(off, on, flipOn);
        offToOn.addActionListener(mockOnListener);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        //make sure no more extra events
        mockOffListener.expectNoMethodCalls();
        mockOnListener.expectNoMethodCalls();
    }

    public void testBasic() {
        Memento memento = sm.createMementoFromState("id", on);

        //fire turn off
        sm.fireEvent(memento, flipOff);

        mockOnListener.expectNoMethodCalls();
        mockOffListener.expectOneMethodCall();

        //fire turn off again...
        sm.fireEvent(memento, flipOff);

        mockOnListener.expectNoMethodCalls();
        mockOffListener.expectNoMethodCalls();

        //fire turn on.....
        sm.fireEvent(memento, flipOn);

        mockOnListener.expectOneMethodCall();
        mockOffListener.expectNoMethodCalls();
    }

    /**
     * This makes sure an Exception causes the statemachine to not get corrupted.  This covers a
     * bug we had where an exception would not allow future firing into statemachine.
     *
     */
    public void testExceptionHandled() {
        Memento memento = sm.createMementoFromState("id", on);

        mockOffListener.addThrowException(() -> {
        	throw new IllegalMonitorStateException();
        });

        CompletableFuture<State> future = sm.fireAsyncEvent(memento, flipOff);
        try {
            //fire turn off
            future.get();
            fail("Should have thrown exception");
        } catch (InterruptedException e) {
        	fail("what");
		} catch (ExecutionException e) {
        	log.info("This exception is expected");
		}

        mockOffListener.expectOneMethodCall();

        //should now be able to fire in to statemachine still!!!!!
        sm.fireEvent(memento, flipOff);

        mockOnListener.expectNoMethodCalls();
        mockOffListener.expectOneMethodCall();
    }

    public void testOrder()
    {
        Memento memento = sm.createMementoFromState("id", on);

        MockFakeInterface mockFake = new MockFakeInterface();
        onToOff.addActionListener(new FakeListener1(mockFake));
        onToOff.addActionListener(new FakeListener2(mockFake));

        //fire turn off
        sm.fireEvent(memento, flipOff);
        
        mockOffListener.expectOneMethodCall();
        mockFake.expectCalls("first", "second");
    }

    public interface FakeInterface
    {
        public void first();
        public void second();
    }

    private class FakeListener1 implements ActionListener
    {
        private FakeInterface fake;

        public FakeListener1(FakeInterface fake)
        {
            this.fake = fake;
        }

        public void actionPerformed(ActionEvent e)
        {
            fake.first();
        }
    }

    private class FakeListener2 implements ActionListener
    {
        private FakeInterface fake;

        public FakeListener2(FakeInterface fake)
        {
            this.fake = fake;
        }

        public void actionPerformed(ActionEvent e)
        {
            fake.second();
        }
    }
}
