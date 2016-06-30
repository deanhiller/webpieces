package org.webpieces.compiler.anonymous;

import java.util.concurrent.Callable;

import org.webpieces.compiler.impl.test.ForTestRouteId;

public class AnonymousController {

	public Callable<ForTestRouteId> getRunnable() {
		return new Callable<ForTestRouteId>() {
			@Override
			public ForTestRouteId call() throws Exception {
				return AnonymousRouteId.GET_SHOW_USER;
			}
		};
	}
}
