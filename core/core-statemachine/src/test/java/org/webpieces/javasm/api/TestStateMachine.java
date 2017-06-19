package org.webpieces.javasm.api;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import junit.framework.TestCase;

/**
 */
public class TestStateMachine extends TestCase
{
	private static final Logger log = Logger.getLogger(TestStateMachine.class.getName());
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

        Transition offToOn = sm.createTransition(off, on, flipOn);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testBasic() {
        Memento memento = sm.createMementoFromState("id", on);

        //fire turn off
        sm.fireEvent(memento, flipOff);

        //fire turn off again...
        sm.fireEvent(memento, flipOff);

        //fire turn on.....
        sm.fireEvent(memento, flipOn);
    }

    public void testOrder()
    {
        Memento memento = sm.createMementoFromState("id", on);

        MockFakeInterface mockFake = new MockFakeInterface();

        //fire turn off
        sm.fireEvent(memento, flipOff);
        
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
