package org.playorm.nio.api.channels;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * This is the top of the tree where all our channels come from. 
 * <pre>
 * TCPServerChannel and TCPChannel have similar functions like 
 * 1. bind
 * 2. isBound
 * 3.
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
 * </pre>
 * 
 * @author Dean Hiller
 *
 */
public interface RegisterableChannel {
	
	/**
	 * Well, Socket, ServerSocket, and DatagramSocket all have this method,
	 * so I put it in a common interface.  Some subclasses may not have
	 * implementations of this method.
	 * @param b
	 */
	void setReuseAddress(boolean b);	
	
    /**
     * The name of the channel shows up in the management GUI if there is one such that an admin
     * can kill clients based on the name.  Username is a great thing to use for the name of a channel
     * so the admin can kill clients based on the username.  He can also monitor usage as well
     * 
     * The name is also used in the logs so if something goes wrong, you know which channel it was.
     * @param string
     */
    public void setName(String string);
    
    public String getName();
    
	/**
	 * @param addr
	 */
	public void bind(SocketAddress addr);
	
	public boolean isBlocking();
	
	public boolean isClosed();
	
	/**
	 * @return true if this channel is bound, and false otherwise.
	 */
	public boolean isBound();
	
	/**
	 * @return the local InetSocketAddress
	 */
	public InetSocketAddress getLocalAddress();		
}
