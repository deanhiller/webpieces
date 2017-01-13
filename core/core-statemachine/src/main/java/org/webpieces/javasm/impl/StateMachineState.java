package org.webpieces.javasm.impl;

import java.util.concurrent.Executor;

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.State;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.webpieces.util.locking.PermitQueue;

/**
 */
public class StateMachineState implements Memento
{
    private static final long serialVersionUID = 1L;

    private Logger log;
    private String id;
    private State state;
    private StateMachineImpl stateMachine;
    private PermitQueue<State> permitQueue;

    public StateMachineState(Executor executor, String rawMapId, String stateMachineId, State state, StateMachineImpl sm)
    {
        this.id = rawMapId+","+stateMachineId;
        this.state = state;
        this.stateMachine = sm;
        String name = Memento.class.getPackage().getName();
        log = LoggerFactory.getLogger(name+"."+rawMapId+"."+stateMachineId);
        permitQueue = new PermitQueue<>(executor, 1);
    }

    @Override
    public String toString() {
        return "["+id+"] ";
    }

    public State getCurrentState()
    {
        return state;
    }

    public void setCurrentState(State state)
    {
        if(state == null)
            throw new IllegalArgumentException("name cannot be null");
        this.state = state;
    }

    public StateMachineImpl getStateMachine()
    {
        return stateMachine;
    }

    public Logger getLogger()
    {
        return log;
    }

	public PermitQueue<State> getPermitQueue() {
		return permitQueue;
	}

}
