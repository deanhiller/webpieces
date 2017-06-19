package org.webpieces.javasm.impl;

import org.webpieces.javasm.api.Transition;

/**
 */
public class TransitionImpl implements Transition
{
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
     * @return Returns the endState.
     */
    public StateImpl getEndState()
    {
        return endState;
    }

	@Override
	public String toString() {
		return "TransitionImpl [endState=" + endState + "]";
	}

}
