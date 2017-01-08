package org.webpieces.javasm.impl;

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.State;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

/**
 */
public class StateMachineState implements Memento
{
    private static final long serialVersionUID = 1L;

    private Logger log;
    private String id;
    private State state;
    private transient boolean inProcess = false;
    private StateMachineImpl stateMachine;

    public StateMachineState(String rawMapId, String stateMachineId, State state, StateMachineImpl sm)
    {
        this.id = rawMapId+","+stateMachineId;
        this.state = state;
        this.stateMachine = sm;
        String name = Memento.class.getPackage().getName();
        log = LoggerFactory.getLogger(name+"."+rawMapId+"."+stateMachineId);
    }

    @Override
    public String toString() {
        return "["+id+"] ";
    }

    public State getCurrentState()
    {
        return state;
    }

    public void setCurrentStateName(State name)
    {
        if(name == null)
            throw new IllegalArgumentException("name cannot be null");
        this.state = name;
    }

    public boolean isInProcess()
    {
        return inProcess;
    }

    public void setInProcess(boolean inProcess)
    {
        this.inProcess = inProcess;
    }

    public StateMachineImpl getStateMachine()
    {
        return stateMachine;
    }

    public Logger getLogger()
    {
        return log;
    }
}
