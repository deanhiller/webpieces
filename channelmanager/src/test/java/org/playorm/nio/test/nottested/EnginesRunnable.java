package org.playorm.nio.test.nottested;

import org.playorm.nio.api.libs.AsyncSSLEngine;

public interface EnginesRunnable extends Runnable {

	public AsyncSSLEngine getEngine();
	
}
