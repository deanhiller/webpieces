package org.webpieces.javasm.api;

import java.awt.event.ActionListener;

import junit.framework.TestCase;

/**
 */
public class TestEntryExitListeners extends TestCase
{
    private StateMachine sm;
    private String flipOn;
    private String flipOff;
    private State on;
    private State off;

    /**
     * Creates an instance of TestStateMachine.
     * @param arg0
     */
    public TestEntryExitListeners(String arg0)
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
        sm = factory.createStateMachine("TestEntryExitListeners");
        
        flipOn = "flipOn";
        flipOff = "flipOff";
        
        on = sm.createState("on");
        off = sm.createState("off");
        
        sm.createTransition(on, off, flipOff);
        
        sm.createTransition(off, on, flipOn);
    }

    /**
     * @see junit.framework.TestCase#tearDown()
     */
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }

    public void testEntryListener()
    {
        Memento memento = sm.createMementoFromState("id", off);
        
        //fire flipOn and enter state on
        sm.fireEvent(memento, flipOn);
    }
    
    public void testExitListener()
    {
        Memento memento = sm.createMementoFromState("id", on);
        
        //fire flipOff and exit state on
        sm.fireEvent(memento, flipOff);

    }
}
