package org.webpieces.compiler.anonymous;

import java.util.concurrent.Callable;

public class SomeRouterModule {

	public Callable<Integer> getRunnable() {
		return new Callable<Integer>() {
			@Override
			public Integer call() throws Exception {
				return 55;
			}
		};
	}
}
