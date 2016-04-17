package org.webpieces.nio.api.channels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.FutureOperation;


/**
 * 
 * TCPServerChannel ->       RegisterableChannel
 * TCPChannel -> Channel ->  RegisterableChannel
 * UDPChannel -> Channel ->  RegisterableChannel
 * DatagramChannel  ->       RegisterableChannel
 * 
 * This is the superclass for UDPChannel and TCPChannel 
 * <pre>
 * TCPServerChannel and TCPChannel have similar functions like 
 * 1. bind
 * 2. isBound
 * 
 * TCPChannel and UDPChannel have similar functions like
 * 1. registerForRead
 * 2. connect (in java, udp has a connect for better point to point performance)
 * 3. bind
 * 4. isBound
 * 
 * This implies the superinterface of UDPChannel and TCPChannel share the
 * same superinterface as TCPServerChannel
 * 
 * TCPServerChannel ->       RegisterableChannel
 * TCPChannel -> Channel ->  RegisterableChannel
 * UDPChannel -> Channel ->  RegisterableChannel
 * DatagramChannel  ->       RegisterableChannel
 * </pre>
 * 
 * @author Dean Hiller
 */
public interface Channel extends RegisterableChannel {

	public FutureOperation connect(SocketAddress addr);
	public FutureOperation write(ByteBuffer b);
	public FutureOperation close();
	
    /**
     * Registers a DataListener that will be notified of all incoming data.  If the threadpool layer setup,
     * requests from clients may come out of order unless you install your own executorService.
     * 
     */
    public void registerForReads(DataListener listener);

    /**
     * Unregister the previously registered DataListener so incoming data is not fired to the client.
     * 
     * @throws IOException
     * @throws InterruptedException
     */
    public void unregisterForReads();
    
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
