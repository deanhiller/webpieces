package org.webpieces.javasm.api;

import java.util.concurrent.Executor;

import org.webpieces.javasm.impl.StateMachineFactoryImpl;


/**
 */
public abstract class StateMachineFactory
{
	public static final String ANY_EVENT = "___ANY";

    public static StateMachineFactory createFactory() {
        return new StateMachineFactoryImpl(); 
    }

    public abstract StateMachine createStateMachine(Executor backedUpThreadPool);

    public abstract StateMachine createStateMachine(Executor backedUpThreadPool, String id);
    
}
