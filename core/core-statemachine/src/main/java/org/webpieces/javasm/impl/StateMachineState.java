package org.webpieces.javasm.impl;

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.State;
import org.webpieces.util.locking.FuturePermitQueue;

/**
 */
public class StateMachineState implements Memento
{
    private static final long serialVersionUID = 1L;

    private String id;
    private State state;
    private StateMachineImpl stateMachine;
    private FuturePermitQueue permitQueue;

    public StateMachineState(String stateMachineId, State state, StateMachineImpl sm)
    {
        this.state = state;
        this.stateMachine = sm;
        this.id = stateMachineId;
        permitQueue = new FuturePermitQueue(stateMachineId, 1);
    }

    @Override
    public String toString() {
        return "["+id+", state="+state+"] ";
    }

    public State getCurrentState()
    {
        return state;
    }

    public void setCurrentState(State state)
    {
        if(state == null)
            throw new IllegalArgumentException(id+" name cannot be null");
        this.state = state;
    }

    public StateMachineImpl getStateMachine()
    {
        return stateMachine;
    }

	public FuturePermitQueue getPermitQueue() {
		return permitQueue;
	}
	
}
