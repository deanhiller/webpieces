package org.webpieces.router.api.exceptions;

import java.util.concurrent.CompletionException;

import org.webpieces.router.impl.routers.MatchInfo;

/**
 * Wraps a generic exception from the specific router 
 * @author dhiller
 *
 */
public class SpecificRouterInvokeException extends CompletionException {

	private static final long serialVersionUID = 1L;
	
	private final MatchInfo matchInfo;

	public SpecificRouterInvokeException(MatchInfo matchInfo, Throwable e) {
		super("Exception invoking route.  See 'Caused by' Exception below", e);
		this.matchInfo = matchInfo;
	}

	public MatchInfo getMatchInfo() {
		return matchInfo;
	}

}
