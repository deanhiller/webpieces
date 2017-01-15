/**
 */
package org.webpieces.javasm.api;

import junit.framework.TestCase;

/**
 */
public class TestMultieventTransition extends TestCase
{
    private MockActionListener mockOffListener = new MockActionListener();
    private MockActionListener mockOnListener = new MockActionListener();
    private StateMachine sm;
    private String flipOn;
    private String alsoFlipOn;
    private String flipOff;
    private State on;
    private Transition onToOff;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();

        StateMachineFactory factory = StateMachineFactory.createFactory();
        sm = factory.createStateMachine("TestMultieventTransition");

        flipOn = "flipOn";
        alsoFlipOn = "alsoFlipOn";
        flipOff = "flipOff";

        on = sm.createState("on");
        State off = sm.createState("off");

        onToOff = sm.createTransition(on, off, flipOff);
        onToOff.addActionListener(mockOffListener);

        Transition offToOn = sm.createTransition(off, on, flipOn, alsoFlipOn);
        offToOn.addActionListener(mockOnListener);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        //make sure no more extra events
        mockOffListener.expectNoMethodCalls();
        mockOnListener.expectNoMethodCalls();
    }

    public void testDifferentEvents()
    {
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
        
        //fire turn off again...
        sm.fireEvent(memento, flipOff);

        mockOnListener.expectNoMethodCalls();
        mockOffListener.expectOneMethodCall();
        
        // and turn back on with the other event
        sm.fireEvent(memento, alsoFlipOn);

        mockOnListener.expectOneMethodCall();
        mockOffListener.expectNoMethodCalls();
        
        //fire turn off again...
        sm.fireEvent(memento, flipOff);

        mockOnListener.expectNoMethodCalls();
        mockOffListener.expectOneMethodCall();

        //fire turn on.....
        sm.fireEvent(memento, flipOn);

        mockOnListener.expectOneMethodCall();
        mockOffListener.expectNoMethodCalls();
    }
}
