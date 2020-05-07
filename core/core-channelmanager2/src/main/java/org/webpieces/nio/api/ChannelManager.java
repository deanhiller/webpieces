package org.webpieces.nio.api;

import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DatagramListener;

/**
 * @author Dean Hiller
 */
public interface ChannelManager {

	/**
	 * Key specific to the Basic Channel Manager and only needs to be set on the
	 * basic channel manager.  Since basic is always the last child, every other
	 * channelmanager will use the value of KEY_ID through the basic channelmanager
	 */
	public static final String KEY_ID = "channelmanager.id";
	/**
	 * Key specific to Basic and Threaded ChannelManager
	 */
	public static final String KEY_BUFFER_FACTORY = "key.bytebuffer.factory";
	
    /**
     * Returns a TCPServerChannel that can listen for incoming TCPChannels
     * 
     * @param id (Should not be null)Used for logging purposes. 
     * @param connectionListener The listener that is notified every time a Channel connects in
     * 
     * @return a TCPServerChannel
     */
    public TCPServerChannel createTCPServerChannel(String id, ConnectionListener connectionListener);

    public TCPServerChannel createTCPServerChannel(
    		String id, ConnectionListener connectionListener, SSLEngineFactory factory);

    public TCPServerChannel createTCPUpgradableChannel(String id, ConnectionListener connectionListener, SSLEngineFactory factory);

    /**
     * Returns a non-blocking TCPChannel.
     * @param id (Should not be null)Used for logging purposes.
     *  
     * @return a non-blocking TCPChannel.
     */
    public TCPChannel createTCPChannel(String id);

    public TCPChannel createTCPChannel(String id, SSLEngine engine);
    
    /**
     * Creates a UDPChannel that can connect to a peer and receive/send data from/to
     * that peer.  We will have to test this, but I hear this is more
     * performance than using the UDPServerChannel
     * 
     * @param id
     *  
     */
    public UDPChannel createUDPChannel(String id);
	
	/*
	 * Creates a UDPServerChannel that can send/receive data from multiple peers.
	 */
	public DatagramChannel createDatagramChannel(String id, int bufferSize, DatagramListener listener);
    
	public void stop();
	
	public String getName();

}
