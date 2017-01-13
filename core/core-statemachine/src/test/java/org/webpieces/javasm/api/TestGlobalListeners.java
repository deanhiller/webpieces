package org.webpieces.javasm.api;

import java.awt.event.ActionListener;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import junit.framework.TestCase;

/**
 */
public class TestGlobalListeners extends TestCase
{
    private MockActionListener beforeCreateStateEntryList = new MockActionListener();
    private MockActionListener afterCreateStateEntryList = new MockActionListener();
    private StateMachine sm;
    private String flipOn;
    private String flipOff;
    private State on;
    private MockActionListener beforeCreateStateExitList = new MockActionListener();
    private MockActionListener afterCreateStateExitList = new MockActionListener();

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
        
        Executor executor = Executors.newFixedThreadPool(2);
        StateMachineFactory factory = StateMachineFactory.createFactory();
        sm = factory.createStateMachine(executor, "TestGlobalListeners");
        
        sm.addGlobalStateEntryAction((ActionListener)beforeCreateStateEntryList);
        sm.addGlobalStateExitAction((ActionListener)beforeCreateStateExitList);
        
        flipOn = "flipOn";
        flipOff = "flipOff";
        
        on = sm.createState("on");
        State off = sm.createState("off");
        
        sm.addGlobalStateEntryAction((ActionListener)afterCreateStateEntryList);
        sm.addGlobalStateExitAction((ActionListener)afterCreateStateExitList);
        
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

        beforeCreateStateEntryList.expectOneMethodCall();
        beforeCreateStateExitList.expectOneMethodCall();
        afterCreateStateEntryList.expectOneMethodCall();
        afterCreateStateExitList.expectOneMethodCall();

        //fire turn off again results in no events...
        sm.fireEvent(memento, flipOff);
        
        beforeCreateStateEntryList.expectNoMethodCalls();
        beforeCreateStateExitList.expectNoMethodCalls();
        afterCreateStateEntryList.expectNoMethodCalls();
        afterCreateStateExitList.expectNoMethodCalls();
        
        //flip on now
        sm.fireEvent(memento, flipOn);
        
        beforeCreateStateEntryList.expectOneMethodCall();
        beforeCreateStateExitList.expectOneMethodCall();
        afterCreateStateEntryList.expectOneMethodCall();
        afterCreateStateExitList.expectOneMethodCall();
    }
    
}
