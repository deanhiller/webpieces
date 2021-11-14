package org.webpieces.nio.api.channels;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import org.webpieces.util.futures.XFuture;

/**
 * 
 * TCPServerChannel -       RegisterableChannel
 * TCPChannel - Channel -  RegisterableChannel
 * UDPChannel - Channel -  RegisterableChannel
 * DatagramChannel  -       RegisterableChannel
 * 
 * This is the top of the tree where all our channels come from. 
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
 * TCPServerChannel -       RegisterableChannel
 * TCPChannel - Channel -  RegisterableChannel
 * UDPChannel - Channel -  RegisterableChannel
 * DatagramChannel  -       RegisterableChannel
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

	public String getChannelId();
    
	/**
	 * @param addr
	 * @return 
	 */
	public XFuture<Void> bind(SocketAddress addr);
	
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
