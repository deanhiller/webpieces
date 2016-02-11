package org.playorm.nio.api;

import java.io.IOException;

import org.playorm.nio.api.channels.DatagramChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.channels.UDPChannel;


public interface ChannelManager {

    /**
     * Returns a TCPServerChannel that can listen for incoming TCPChannels
     * 
     * @param id (Should not be null)Used for logging purposes. 
     *  @param settings Can be null.  This is used when you want to pass a SSLEngineFactory or 
     *                   PacketProcessorFactory down to the ssl and packet layers.  
     * @return a TCPServerChannel
     */
    public TCPServerChannel createTCPServerChannel(String id);   

    /**
     * Returns a non-blocking TCPChannel.
     * @param id (Should not be null)Used for logging purposes. 
     * @param h (Can be null)The Settings holds factories that turn on security or packetizing.  If
     *          h is null, or the factory is null, that feature will not be turned on.  The layer for that
     *          feature must be in the ChannelManager stack also to be turned on.
     * @return a non-blocking TCPChannel.
     */
    public TCPChannel createTCPChannel(String id);

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
    public UDPChannel createUDPChannel(String id);   
	
	/*
	 * Creates a UDPServerChannel that can send/receive data from multiple peers.
	 */
	public DatagramChannel createDatagramChannel(String id, int bufferSize);
    
	public void stop();
}
