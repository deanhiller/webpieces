package org.webpieces.frontend2.api;

public interface StreamListener {

	HttpStream openStream(FrontendSocket onSocket);

	/**
	 * 1. Socket just closed OR
	 * 2. your in http2 and GoAway was sent and socket is closing
	 * 3. This is called before cancelling all streams in http1.1.
	 * 4. TODO(dhiller): Need to test in http2 and make sure cancel all streams is done the same as http1.1(not sure)  
	 * 
	 * @param socketThatClosed
	 */
	void fireIsClosed(FrontendSocket socketThatClosed);
	
}
