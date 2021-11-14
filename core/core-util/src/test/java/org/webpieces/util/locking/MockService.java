package org.webpieces.util.locking;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;

public class MockService {

	private List<XFuture<Long>> toReturn = new ArrayList<>();
	private List<Integer> params = new ArrayList<>();
	
	public XFuture<Long> runFunction(int i) {
		if(toReturn.size() == 0)
			throw new IllegalArgumentException("not enough return values provided");
		
		params.add(i);
		return toReturn.remove(0);
	}

	public void addToReturn(XFuture<Long> future1) {
		toReturn.add(future1);
	}

	public List<Integer> getAndClear() {
		List<Integer> temp = params;
		params = new ArrayList<>();
		return temp;
	}	
	
}
