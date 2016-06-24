package org.webpieces.nio.api.channels;

import java.nio.channels.ServerSocketChannel;

import org.webpieces.nio.api.handlers.ConsumerFunc;

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
    
    /**
     * This should be called before bind if you are going to configure properties and then
     * you can either call serverSocketChannel.setOption or serverSocketChannel.getSocket().setReuse, setXXX, etc.
     * 
     * @param methodToConfigure
     */
	public void configure(ConsumerFunc<ServerSocketChannel> methodToConfigure);

	public ServerSocketChannel getUnderlyingChannel();
}
