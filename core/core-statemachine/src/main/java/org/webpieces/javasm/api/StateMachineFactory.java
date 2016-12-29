package org.webpieces.javasm.api;

import org.webpieces.javasm.impl.StateMachineFactoryImpl;


/**
 */
public abstract class StateMachineFactory
{
	public static final String ANY_EVENT = "___ANY";

    public static StateMachineFactory createFactory() {
        return new StateMachineFactoryImpl(); 
    }

    public abstract StateMachine createStateMachine();

    public abstract StateMachine createStateMachine(String id);
    
}
