package org.webpieces.javasm.impl;

import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.webpieces.javasm.api.Memento;
import org.webpieces.javasm.api.State;
import org.webpieces.javasm.api.StateMachine;
import org.webpieces.javasm.api.Transition;

/**
 */
public class StateMachineImpl implements StateMachine
{
    private final Map<String, StateImpl> nameToState = new HashMap<String, StateImpl>();
    private final EventListenerList globalEntryListeners = new EventListenerList();
    private final EventListenerList globalExitListeners = new EventListenerList();
    private final String rawMapId;

    /**
     * Creates an instance of StateMachineImpl.
     * @param executor 
     * @param id
     */
    public StateMachineImpl(String id)
    {
		if(id == null)
            rawMapId = "unnamed";
        else
            rawMapId = id;
    }

    public Memento createMementoFromState(String stateMachineId, State state) {
        State name = nameToState.get(state.getName());
        if(name == null)
            throw new IllegalArgumentException(this + "This state does not exist in this statemachine.  name="+name);
        return new StateMachineState(rawMapId, stateMachineId, state, this);
    }

    /**
     * @see org.webpieces.javasm.api.StateMachine#createState(java.lang.String)
     */
    public State createState(String name)
    {
    	StateImpl state = nameToState.get(name);
    	if(state != null)
    		throw new IllegalArgumentException("This state already exists. You can't create the same state twice");
        state = new StateImpl(name);
        nameToState.put(name, state);
        for(ActionListener l : globalEntryListeners.getListeners(ActionListener.class)) {
            state.addEntryActionListener(l);
        }

        for(ActionListener l : globalExitListeners.getListeners(ActionListener.class)) {
            state.addExitActionListener(l);
        }
        return state;
    }

    public Transition createTransition(State[] startStates, State endState, Object... events)
    {
        if(events.length < 1)
        {
            throw new IllegalArgumentException(this + "You must specify at least one event");
        }
        else if(!(endState instanceof StateImpl))
        {
            throw new IllegalArgumentException(rawMapId + "endState are not created using this StateMachine");
        }   if(startStates.length < 1)
        {
            throw new IllegalArgumentException(rawMapId + "You must specify at least one event");
        }


        TransitionImpl transition = new TransitionImpl((StateImpl)endState);
        for(State startState : startStates)
        {
            StateImpl startImpl = (StateImpl)startState;
            for(Object event : events)
            {
                startImpl.addTransition(event, transition);
            }
        }
        return transition;
    }
    /**
     * @see org.webpieces.javasm.api.StateMachine#createTransition(org.webpieces.javasm.api.State, org.webpieces.javasm.api.State, org.webpieces.javasm.api.Event[])
     */
    public Transition createTransition(State startState, State endState, Object... events)
    {
        State[] startStates = {startState};
        return createTransition(startStates, endState, events);

    }
    
    @Override
    public State fireEvent(Memento memento, Object evt)
    {
        if(memento == null)
            throw new IllegalArgumentException(this + "memento cannot be null");
        else if(evt == null)
            throw new IllegalArgumentException(this + "evt cannot be null");
        else if(!(memento instanceof StateMachineState))
            throw new IllegalArgumentException(this + "memento was not created using StateMachine.createMementoFromIntialState and must be");
        else if( ((StateMachineState)memento).getStateMachine() != this)
            throw new IllegalArgumentException(this + "memento was not created with this specific statemachine.  " +
                    "you got your statemachines mixed up with the mementos");

        StateMachineState smState = (StateMachineState)memento;
        
        return fire(evt, smState);
    }

	private State fire(Object evt, StateMachineState smState) {
		try {
            //get the current state
            StateImpl state = nameToState.get(smState.getCurrentState().getName());
            state.fireEvent(smState, evt);
            return smState.getCurrentState();
        } catch(RuntimeException e) {
            //NOTE: Stack trace is not logged here.  That is the responsibility of the javasm client
            //so exceptions don't get logged multiple times.
            smState.getLogger().warn(this+"Exception occurred going out of state="+smState.getCurrentState()+", event="+evt);
            throw e;
        }
	}

    /**
     * @see org.webpieces.javasm.api.StateMachine#addGlobalStateEntryAction(java.awt.event.ActionListener)
     */
    public StateMachine addGlobalStateEntryAction(ActionListener l)
    {
        //first add it to all created states
        for(State state : nameToState.values()) {
            state.addEntryActionListener(l);
        }
        globalEntryListeners.add(ActionListener.class, l);
        return this;
    }

    /**
     * @see org.webpieces.javasm.api.StateMachine#addGlobalStateExitAction(java.awt.event.ActionListener)
     */
    public StateMachine addGlobalStateExitAction(ActionListener l)
    {
        //first add it to all created states
        for(State state : nameToState.values()) {
            state.addExitActionListener(l);
        }
        globalExitListeners.add(ActionListener.class, l);
        return this;
    }

    @Override
    public String toString() {
        return "[" + rawMapId + "] ";
    }
}
