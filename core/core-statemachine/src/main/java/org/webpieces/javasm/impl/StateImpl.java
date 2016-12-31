package org.webpieces.javasm.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.event.EventListenerList;

import org.webpieces.javasm.api.NoTransitionListener;
import org.webpieces.javasm.api.State;
import org.webpieces.javasm.api.StateMachineFactory;

/**
 */
public class StateImpl implements State
{
    private String name;
    private Map<Object, TransitionImpl> evtToTransition = new HashMap<Object, TransitionImpl>();
    private EventListenerList entryListeners = new EventListenerList();
    private EventListenerList exitListeners = new EventListenerList();
    private EventListenerList noTransitionListeners = new EventListenerList();

    /**
     * Creates an instance of StateImpl.
     * @param name
     */
    public StateImpl(String name)
    {
        this.name = name;
    }


    @Override
    public String toString() {
        //TODO: return all the events and transition to next states???
        return name;
    }


    /**
     * @param evt
     * @param transition
     */
    public void addTransition(Object evt, TransitionImpl transition)
    {
        TransitionImpl t = evtToTransition.get(evt);
        if(t != null)
            throw new IllegalArgumentException("A transition our of state="+this
                  +" caused from evt="+evt+" has already been added.  Cannot add another one.");
        
        evtToTransition.put(evt, transition);
    }


    /**
     * @param smState
     * @param evt
     */
    public void fireEvent(StateMachineState smState, Object evt)
    {
        TransitionImpl transition = evtToTransition.get(evt);
        if(transition == null) {
            transition = evtToTransition.get(StateMachineFactory.ANY_EVENT);
        }
        if(transition == null) {
//            log.fine(smState+"CurrentState="+name+" evt="+evt+" no transition found");
            smState.getLogger().debug(() -> smState+"No Transition: "+getName()+" -> <no transition found>, event="+evt);
            
            this.fireNoTransition(smState, evt);
            return;
        }

        StateImpl endState = transition.getEndState();
        String nextState = transition.getEndState().getName();
        smState.getLogger().debug(() -> smState+"Transition: "+getName()+" -> "+nextState+", event="+evt);

        try {
            this.fireExitActions(smState);
            transition.fireTransitionActions(smState);
            endState.fireEntryActions(smState);

            smState.setCurrentStateName(nextState);
        } catch(RuntimeException e) {
            smState.getLogger().warn(smState+"Transition FAILED: "+getName()+" -> "+nextState+", event="+evt);
            throw e;
        }
    }

    private void fireNoTransition(StateMachineState smState, Object event) {
    	NoTransitionListener[] list = noTransitionListeners.getListeners(NoTransitionListener.class);
        for(int ii = list.length-1; ii >= 0; ii--) {
        	int index = ii;
            smState.getLogger().debug(()->smState+"Exit Action: "+list[index].getClass().getName()+", state="+getName());
            list[ii].noTransitionFromEvent(this, event);
        }		
	}


	/**
     * @param smState
     */
    private void fireEntryActions(StateMachineState smState)
    {
        ActionListener[] list = entryListeners.getListeners(ActionListener.class);
        ActionEvent evt = new ActionEvent(this, 0, null);
        for(int ii = list.length-1; ii >= 0; ii--) {
            try {
            	int index = ii;
                smState.getLogger().debug(() -> smState+"Entry Action: "+list[index].getClass().getName()+", state="+getName());
                list[ii].actionPerformed(evt);
            } catch(RuntimeException e) {
                //Do not log stack trace here.  It should only be logged at the beginning of a thread
                smState.getLogger().warn(smState+"Exception occurred in client ActionListener="+list[ii]+", state="+getName());
                //rethrow and stop executing the rest of the Actions
                throw e;
            }
        }
    }


    /**
     * @param smState
     */
    private void fireExitActions(StateMachineState smState)
    {
        ActionListener[] list = exitListeners.getListeners(ActionListener.class);
        ActionEvent evt = new ActionEvent(this, 0, null);
        for(int ii = list.length-1; ii >= 0; ii--) {
            try {
            	int index = ii;
                smState.getLogger().debug(()->smState+"Exit Action: "+list[index].getClass().getName()+", state="+getName());
                list[ii].actionPerformed(evt);
            } catch(RuntimeException e) {
                smState.getLogger().warn(smState+"Exception occurred in client ActionListener="+list[ii]+", state="+getName());
                //rethrow and stop executing the rest of the Actions
                throw e;
            }
        }
    }


    /**
     */
    public String getName()
    {
        return name;
    }


    /**
     * @see org.webpieces.javasm.api.State#addEntryActionListener(java.awt.event.ActionListener)
     */
    public State addEntryActionListener(ActionListener listener)
    {
        if(listener == null)
            throw new IllegalArgumentException("listener cannot be null");
        entryListeners.add(ActionListener.class, listener);
        return this;
    }


    /**
     * @see org.webpieces.javasm.api.State#addExitActionListener(java.awt.event.ActionListener)
     */
    public State addExitActionListener(ActionListener listener)
    {
        if(listener == null)
            throw new IllegalArgumentException("listener cannot be null");
        exitListeners.add(ActionListener.class, listener);
        return this;
    }


	@Override
	public State addNoTransitionListener(NoTransitionListener listener) {
        if(listener == null)
            throw new IllegalArgumentException("listener cannot be null");
        noTransitionListeners.add(NoTransitionListener.class, listener);
        return this;
	}
}
