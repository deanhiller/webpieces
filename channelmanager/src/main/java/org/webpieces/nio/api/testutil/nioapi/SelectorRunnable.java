package org.webpieces.nio.api.testutil.nioapi;

import java.nio.channels.ClosedChannelException;

public interface SelectorRunnable {

	void run() throws ClosedChannelException;

}
