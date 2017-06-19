package org.webpieces.javasm.api;

import junit.framework.TestCase;

/**
 */
public class TestGlobalListeners extends TestCase
{
    private StateMachine sm;
    private String flipOn;
    private String flipOff;
    private State on;

    /**
     * Creates an instance of TestStateMachine.
     * @param arg0
     */
    public TestGlobalListeners(String arg0)
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
        sm = factory.createStateMachine("TestGlobalListeners");
        
        flipOn = "flipOn";
        flipOff = "flipOff";
        
        on = sm.createState("on");
        State off = sm.createState("off");
        
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

    public void testBasic() {
        Memento memento = sm.createMementoFromState("id", on);
        
        //fire turn off
        sm.fireEvent(memento, flipOff);

        //fire turn off again results in no events...
        sm.fireEvent(memento, flipOff);
        
        //flip on now
        sm.fireEvent(memento, flipOn);
        
    }
    
}
