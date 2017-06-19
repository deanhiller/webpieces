package org.webpieces.javasm.api;

/**
 */
public interface State
{
	
	/**
	 * get the name 
	 * @return state name
	 */
	String getName();

	State setNoTransitionListener(NoTransitionListener listener);

}
