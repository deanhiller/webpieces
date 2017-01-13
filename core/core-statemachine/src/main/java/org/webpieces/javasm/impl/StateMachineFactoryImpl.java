package org.webpieces.javasm.impl;

import java.util.concurrent.Executor;

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
    public StateMachine createStateMachine(Executor executor)
    {
        return createStateMachine(executor, null);
    }


    /**
     * @see org.webpieces.javasm.api.StateMachineFactory#createStateMachine(java.lang.String)
     */
    @Override
    public StateMachine createStateMachine(Executor executor, String id)
    {
        return new StateMachineImpl(executor, id);
    }

}
