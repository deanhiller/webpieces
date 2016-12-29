package org.webpieces.javasm.impl;

import org.webpieces.javasm.api.StateMachine;
import org.webpieces.javasm.api.StateMachineFactory;

/**
 */
public class StateMachineFactoryImpl extends StateMachineFactory
{

    /**
     * @see org.webpieces.javasm.api.StateMachineFactory#createStateMachine()
     */
    @Override
    public StateMachine createStateMachine()
    {
        return new StateMachineImpl(null);
    }


    /**
     * @see org.webpieces.javasm.api.StateMachineFactory#createStateMachine(java.lang.String)
     */
    @Override
    public StateMachine createStateMachine(String id)
    {
        return new StateMachineImpl(id);
    }

}
