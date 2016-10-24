package org.webpieces.util.filters;

import java.util.concurrent.CompletableFuture;

public class SomeService implements Service<Integer, String> {

	@Override
	public CompletableFuture<String> invoke(Integer meta) {
		System.out.println("service");
		return CompletableFuture.completedFuture("hi there");
	}

}
