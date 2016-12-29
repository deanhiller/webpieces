package org.webpieces.javasm.api;

import java.util.EventListener;

public interface NoTransitionListener extends EventListener {

	public void noTransitionFromEvent(State state, Object event);
	
}
