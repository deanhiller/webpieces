package org.webpieces.router.impl.loader;

import java.util.concurrent.CompletionException;

import org.webpieces.router.impl.model.MatchResult;

public class HaveRouteException extends CompletionException {

	private static final long serialVersionUID = 1L;

	private MatchResult result;

	public HaveRouteException(MatchResult result, Throwable e) {
		super(e);
		this.result = result;
		
	}

	public MatchResult getResult() {
		return result;
	}

}
