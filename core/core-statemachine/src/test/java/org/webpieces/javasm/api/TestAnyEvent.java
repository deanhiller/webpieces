/**
 */
package org.webpieces.javasm.api;

import junit.framework.TestCase;

/**
 */
public class TestAnyEvent extends TestCase
{
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

        Transition offToOn = sm.createTransition(off, on, StateMachineFactory.ANY_EVENT);
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
    }
    
    public void testAnyEvent()
    {
        Memento memento = sm.createMementoFromState("id", on);

        //fire turn off
        sm.fireEvent(memento, flipOff);

        //fire turn off again...
        sm.fireEvent(memento, flipOff);

        //fire turn on.....
        sm.fireEvent(memento, flipOn);

        //fire turn off again...
        sm.fireEvent(memento, flipOff);

    }
}
