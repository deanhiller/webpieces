package org.webpieces.util.locking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MockService {

	private List<CompletableFuture<Long>> toReturn = new ArrayList<>();
	private List<Integer> params = new ArrayList<>();
	
	public CompletableFuture<Long> runFunction(int i) {
		if(toReturn.size() == 0)
			throw new IllegalArgumentException("not enough return values provided");
		
		params.add(i);
		return toReturn.remove(0);
	}

	public void addToReturn(CompletableFuture<Long> future1) {
		toReturn.add(future1);
	}

	public List<Integer> getAndClear() {
		List<Integer> temp = params;
		params = new ArrayList<>();
		return temp;
	}	
	
}
