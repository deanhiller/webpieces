package org.webpieces.frontend2.api;

import java.net.InetSocketAddress;

import org.webpieces.frontend2.impl.ProtocolType;
import org.webpieces.nio.api.channels.ChannelSession;

public interface FrontendSocket {

	/**
	 * If http/2, reason will be sent to client, otherwise in http1.1, the socket is simply closed with no info
	 */
	void close(String reason);

	ChannelSession getSession();
	
	public ProtocolType getProtocol();
	
	/**
	 * True when we terminate the SSL on this server.  It is false when the firewall terminates the
	 * SSL and then does http to this server!!!  This means you need to have your firewall put in 
	 * headers indicating from ssl channel or not.
	 */
	public boolean isHttps(); 

	/**
	 * Gets the bound server socket address of the server socket that this socket was opened from.  
	 * This MAY NOT match the HOST header which is the address it is being 
	 * sent to(ie. the firewall).  This can help you determine
	 * if it is SSL if isHttps does not work for you
	 */
	public InetSocketAddress getServerLocalBoundAddress();
	
	/**
	 * Gets the local address/port of this socket that was opened which is usually not that interesting
	 */
	public InetSocketAddress getLocalAddress();
	
	/**
	 * Gets the remote address of this socket (which could be your firewall)
	 */
	public InetSocketAddress getRemoteAddress();
	
}
