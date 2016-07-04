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
	 * In the case of SSL, this is called twice, once with isReadyForWrites=false when the socket is connected
	 * and once with isReadyForWrites=true when the encrypted link is established
	 * 
	 * @param channel
	 * @param isReadyForWrites TODO
	 * @param readyForWrites This is true in every case except an SSL Server where connected is called twice, once for
	 * when socket is connected(so you can start timeout timers if desired) and once when the encrypted link is established
	 * and you can write to the socket
	 * @return you must return CompletableFuture.completedFuture(yourDataListenerInstance).
	 * We do this, because in multithread mode, you are in a thread pool, we only want to register for reads on the socket 
	 * once we have this listener.  Without it, we don't want to read data with no where to send that data 
	 */
	public CompletableFuture<DataListener> connected(Channel channel, boolean isReadyForWrites);
	
	/**
	 * Unfortunately, channel may be the TCPServerChannel if accepting and failed or
	 * the TCPChannel when finishConnecting fails.  If doing UDP, it would be the 
	 * UDPChannel every time.
	 * 
	 */
	public void failed(RegisterableChannel channel, Throwable e);
}
