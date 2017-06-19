package org.webpieces.javasm.impl;

import java.util.HashMap;
import java.util.Map;

import org.webpieces.javasm.api.NoTransitionListener;
import org.webpieces.javasm.api.State;
import org.webpieces.javasm.api.StateMachineFactory;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class StateImpl implements State
{
	private static final Logger log = LoggerFactory.getLogger(StateImpl.class);
	
    private String name;
    private Map<Object, TransitionImpl> evtToTransition = new HashMap<Object, TransitionImpl>();
    private NoTransitionListener noTransitionListener;

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
        	log.debug(() -> smState+"No Transition: "+getName()+" -> <no transition found>, event="+evt);
        	if(noTransitionListener != null)
        		noTransitionListener.noTransitionFromEvent(smState.getCurrentState(), evt);
            return;
        }

        State nextState = transition.getEndState();
        if(log.isDebugEnabled())
        	log.debug(() -> smState+"Transition: "+getName()+" -> "+nextState+", event="+evt);

        try {
            smState.setCurrentState(nextState);
        } catch(RuntimeException e) {
            log.warn(smState+"Transition FAILED: "+getName()+" -> "+nextState+", event="+evt);
            throw e;
        }
    }

    /**
     */
    public String getName()
    {
        return name;
    }


	@Override
	public State setNoTransitionListener(NoTransitionListener listener) {
		this.noTransitionListener = listener;
        return this;
	}
}
