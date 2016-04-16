package org.webpieces.asyncserver.api;

import java.nio.ByteBuffer;

public interface AsyncServer {

	public void enableOverloadMode(ByteBuffer overloadResponse);
	
	public void disableOverloadMode();
	
    public void closeServerChannel();
    
}
