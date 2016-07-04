package org.webpieces.nio.api.handlers;


import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;


public interface ConnectionListener {
	
	/**
	 * Return the DataListener that will listen for data.  This could be the same instance for
	 * every channel to save memory since we give you the Channel and the ByteBuffer on every
	 * method of DataListener.  This helps with creating a more stateless based system.
	 * 
	 * You could also return a new DataListener for each Channel if you desire that as well
	 * 
	 * @param channel
	 * @return you must return CompletableFuture.completedFuture(yourDataListenerInstance).
	 * We do this, because you are in a thread pool, we only want to register for reads on the socket 
	 * once we have this listener.  Without it, we don't want to read data with no where to send that data 
	 */
	public CompletableFuture<DataListener> connected(Channel channel);
	
	/**
	 * Unfortunately, channel may be the TCPServerChannel if accepting and failed or
	 * the TCPChannel when finishConnecting fails.  If doing UDP, it would be the 
	 * UDPChannel every time.
	 * 
	 */
	public void failed(RegisterableChannel channel, Throwable e);
}
