package org.webpieces.nio.api.channels;

import java.io.IOException;

import org.webpieces.nio.api.handlers.ConnectionListener;


/**
 * @author Dean Hiller
 */
public interface TCPServerChannel extends RegisterableChannel {
		
    /**
     * Registers a ConnectionListener that will hand your client TCPChannels as clients connect
     * in.  
     * 
     * @param listener The listener that is handed the new TCPChannel
     * @throws IOException
     * @throws InterruptedException
     */
	public void registerServerSocketChannel(final ConnectionListener listener);
	
}
