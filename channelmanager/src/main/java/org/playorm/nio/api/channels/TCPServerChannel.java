package org.playorm.nio.api.channels;

import java.io.IOException;

import org.playorm.nio.api.handlers.ConnectionListener;


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
	
    /**
     * This is a synchronous close (just like all of the other close() methods on other channels).
     * The reason that this is the only one offered is that closing a server channel does not
     * require any network activity, so happens immediately.
     */
    public void oldClose();
}
