package org.webpieces.util.filters;

import org.webpieces.util.futures.XFuture;

public class SomeService implements Service<Integer, String> {

	@Override
	public XFuture<String> invoke(Integer meta) {
		System.out.println("service");
		return XFuture.completedFuture("hi there");
	}

}
