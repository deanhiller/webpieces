package org.webpieces.javasm.api;

import java.awt.event.ActionListener;

/**
 */
public interface State
{
	
	/**
	 * get the name 
	 * @return state name
	 */
	String getName();

    /**
     * @param listener is the action to be performed on any transition into this State.
     */
    State addEntryActionListener(ActionListener listener);

    /**
     * @param listener is the action to be performed on any transition out of this State.
     */
    State addExitActionListener(ActionListener listener);

	State addNoTransitionListener(NoTransitionListener listener);

}
