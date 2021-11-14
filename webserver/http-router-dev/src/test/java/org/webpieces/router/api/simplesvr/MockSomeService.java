package org.webpieces.router.api.simplesvr;

import java.util.ArrayList;
import java.util.List;
import org.webpieces.util.futures.XFuture;

public class MockSomeService extends SomeService {

	private List<XFuture<Integer>> toReturn = new ArrayList<>();
	
	@Override
	public XFuture<Integer> remoteCall() {
		return toReturn.remove(0);
	}
	
	public void addToReturn(XFuture<Integer> val) {
		toReturn.add(val);
	}

}
