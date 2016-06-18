package org.webpieces.router.api.simplesvr;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MockSomeService extends SomeService {

	private List<CompletableFuture<Integer>> toReturn = new ArrayList<>();
	
	@Override
	public CompletableFuture<Integer> remoteCall() {
		return toReturn.remove(0);
	}
	
	public void addToReturn(CompletableFuture<Integer> val) {
		toReturn.add(val);
	}

}
