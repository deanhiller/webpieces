package org.webpieces.nio.api;

import java.io.IOException;

import org.webpieces.nio.api.channels.DatagramChannel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.handlers.DatagramListener;


public interface ChannelManager {

	public void start();
	
	public void stop();
	
    /**
     * Returns a TCPServerChannel that can listen for incoming TCPChannels
     * 
     * @param id (Should not be null)Used for logging purposes. 
     *  @param settings Can be null.  This is used when you want to pass a SSLEngineFactory or 
     *                   PacketProcessorFactory down to the ssl and packet layers.  
     * @return a TCPServerChannel
     */
    public TCPServerChannel createTCPServerChannel(String id, ConnectionListener listener);   

    /**
     * Returns a non-blocking TCPChannel.
     * @param id (Should not be null)Used for logging purposes. 
     * @return a non-blocking TCPChannel.
     */
    public TCPChannel createTCPChannel(String id, DataListener listener);

    /**
     * Creates a UDPChannel that can connect to a peer and receive/send data from/to
     * that peer.  We will have to test this, but I hear this is more
     * performance than using the UDPServerChannel
     * 
     * @param id 
     * @param settings (Can be null)Not used at this time
     * @return a UDPChannel
     * @throws IOException
     */
    public UDPChannel createUDPChannel(String id, DataListener listener);   
	
	/*
	 * Creates a UDPServerChannel that can send/receive data from multiple peers.
	 */
	public DatagramChannel createDatagramChannel(String id, int bufferSize, DatagramListener listener);
    
}
