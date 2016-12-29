/**
 */
package org.webpieces.javasm.api;

import java.awt.event.ActionListener;

import junit.framework.TestCase;

/**
 */
public class TestAnyEvent extends TestCase
{
    private MockActionListener mockOffListener = new MockActionListener();
    private MockActionListener mockOnListener = new MockActionListener();
    private StateMachine sm;
    private String flipOn;
    private String flipOff;
    private State on;
    private Transition onToOff;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        StateMachineFactory factory = StateMachineFactory.createFactory();
        sm = factory.createStateMachine("TestAnyEvent");

        flipOn = "flipOn";
        flipOff = "flipOff";

        on = sm.createState("on");
        State off = sm.createState("off");

        onToOff = sm.createTransition(on, off, StateMachineFactory.ANY_EVENT);
        onToOff.addActionListener((ActionListener)mockOffListener);

        Transition offToOn = sm.createTransition(off, on, StateMachineFactory.ANY_EVENT);
        offToOn.addActionListener((ActionListener)mockOnListener);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        //make sure no more extra events
        mockOffListener.expectNoMethodCalls();
        mockOnListener.expectNoMethodCalls();
    }
    
    public void testAnyEvent()
    {
        Memento memento = sm.createMementoFromState("id", on);

        //fire turn off
        sm.fireEvent(memento, flipOff);

        mockOffListener.expectOneMethodCall();
        mockOnListener.expectNoMethodCalls();

        //fire turn off again...
        sm.fireEvent(memento, flipOff);

        mockOnListener.expectOneMethodCall();
        mockOffListener.expectNoMethodCalls();

        //fire turn on.....
        sm.fireEvent(memento, flipOn);

        mockOffListener.expectOneMethodCall();
        mockOnListener.expectNoMethodCalls();

        //fire turn off again...
        sm.fireEvent(memento, flipOff);

        mockOnListener.expectOneMethodCall();
        mockOffListener.expectNoMethodCalls();
    }
}
