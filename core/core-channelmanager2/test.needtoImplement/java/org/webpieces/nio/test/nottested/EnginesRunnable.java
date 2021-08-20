package org.webpieces.nio.test.nottested;

import org.webpieces.nio.api.libs.AsyncSSLEngine;

public interface EnginesRunnable extends Runnable {

	public AsyncSSLEngine getEngine();
	
}
