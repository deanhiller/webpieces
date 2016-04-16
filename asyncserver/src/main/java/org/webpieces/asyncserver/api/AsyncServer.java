package org.webpieces.asyncserver.api;

import java.nio.ByteBuffer;

import org.webpieces.nio.api.channels.TCPServerChannel;

public interface AsyncServer {

	public void enableOverloadMode(ByteBuffer overloadResponse);
	
	public void disableOverloadMode();
	
    public void closeServerChannel();
    
}
