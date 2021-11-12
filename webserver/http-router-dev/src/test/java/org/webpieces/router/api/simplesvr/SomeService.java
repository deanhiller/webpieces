package org.webpieces.router.api.simplesvr;

import org.webpieces.util.futures.XFuture;

public class SomeService {

	public XFuture<Integer> remoteCall() {
		return XFuture.completedFuture(5);
	}

}
