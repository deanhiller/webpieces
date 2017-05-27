package org.webpieces.javasm.impl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.event.EventListenerList;

import org.webpieces.javasm.api.Transition;

/**
 */
public class TransitionImpl implements Transition
{
    private EventListenerList listeners = new EventListenerList();
    private StateImpl endState;
    
    /**
     * Creates an instance of TransitionImpl.
     * @param endState
     */
    public TransitionImpl(StateImpl endState)
    {
        this.endState = endState;
    }

    /**
     * @see org.webpieces.javasm.api.Transition#addActionListener(java.awt.event.ActionListener)
     */
    public Transition addActionListener(ActionListener listener)
    {
        if(listener == null)
            throw new IllegalArgumentException("listener cannot be null");
        listeners.add(ActionListener.class, listener);
        return this;
    }

    /**
     * @return Returns the endState.
     */
    public StateImpl getEndState()
    {
        return endState;
    }

    /**
     * 
     */
    public void fireTransitionActions(StateMachineState smState)
    {
        ActionListener[] list = listeners.getListeners(ActionListener.class);
        ActionEvent evt = new ActionEvent(this, 0, null);
        for(int ii = list.length-1; ii >= 0; ii--) {
            try {
                int index = ii;
                smState.getLogger().trace(()->smState+"Action: "+list[index].getClass().getName()+", state="+smState.getCurrentState());
                list[ii].actionPerformed(evt);
            } catch(RuntimeException e) {
                smState.getLogger().warn(smState+"Exception occurred in client ActionListener="+list[ii]);
                //rethrow and stop executing the rest of the Actions
                throw e;
            }
        }
    }

	@Override
	public String toString() {
		return "TransitionImpl [endState=" + endState + "]";
	}

}
