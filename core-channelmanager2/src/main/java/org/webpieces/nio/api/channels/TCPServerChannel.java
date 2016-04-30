package org.webpieces.nio.api.channels;

/**
 * @author Dean Hiller
 */
public interface TCPServerChannel extends RegisterableChannel {
		
    /**
     * This is a synchronous close (just like all of the other close() methods on other channels).
     * The reason that this is the only one offered is that closing a server channel does not
     * require any network activity, so happens immediately.
     */
    public void closeServerChannel();
}
