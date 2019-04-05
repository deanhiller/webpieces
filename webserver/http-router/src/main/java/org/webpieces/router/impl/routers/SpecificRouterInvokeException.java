package org.webpieces.router.impl.routers;

import java.util.concurrent.CompletionException;

/**
 * Wraps a generic exception from the specific router 
 * @author dhiller
 *
 */
public class SpecificRouterInvokeException extends CompletionException {

	private static final long serialVersionUID = 1L;
	
	private final MatchInfo matchInfo;

	public SpecificRouterInvokeException(MatchInfo matchInfo, Throwable e) {
		super(e);
		this.matchInfo = matchInfo;
	}

	public MatchInfo getMatchInfo() {
		return matchInfo;
	}

}
