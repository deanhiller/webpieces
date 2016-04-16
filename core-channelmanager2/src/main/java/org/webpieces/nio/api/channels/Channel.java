package org.webpieces.nio.api.channels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.handlers.ChannelSession;
import org.webpieces.nio.api.handlers.FutureOperation;


/**
 * @author Dean Hiller
 */
public interface Channel extends RegisterableChannel {

	public FutureOperation connect(SocketAddress addr);
	public FutureOperation write(ByteBuffer b);
	
    /**
     * This method only needs to be called if you call stopReadingData and need to start reading data
     * again.
     */
    public void startReadingData();

    /**
     * This notifies us to stop reading data which would result in throttling the other end as the local
     * nic buffer fills up, then the remote nic buffer so that client writes would start becoming blocked
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public void stopReadingData();
    
    /**
     * Gets the remote address the channel is communicating with.
     * 
     * @return the remote address the channel is communicating with.
     */
    public InetSocketAddress getRemoteAddress();
    
    /**
     * Returns whether or not the channel is connected.
     * @return whether or not the channel is connected.
     */
    public boolean isConnected();    
    
    /**
     * Each Channel has a ChannelSession where you can store state.  IF you have one client per Socket, then you can
     * easily store client state in the Channel itself so instead of passing around a Session in your code, you can pass
     * around a Channel that has a ChannelSession. 
     * 
     * @return client's Session object
     */
    public ChannelSession getSession();
    
}
