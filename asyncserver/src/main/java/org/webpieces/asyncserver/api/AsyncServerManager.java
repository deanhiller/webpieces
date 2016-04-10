package org.webpieces.asyncserver.api;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.DataListener;

public interface AsyncServerManager {

	public void start();
	
	public void stop();
	
	public TCPServerChannel createTcpServer(String id, DataListener listener);
	
}
