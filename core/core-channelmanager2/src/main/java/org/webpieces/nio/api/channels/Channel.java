package org.webpieces.nio.api.channels;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.handlers.DataListener;


/**
 * 
 * TCPServerChannel -       RegisterableChannel
 * TCPChannel - Channel -  RegisterableChannel
 * UDPChannel - Channel -  RegisterableChannel
 * DatagramChannel  -       RegisterableChannel
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
 * TCPServerChannel -       RegisterableChannel
 * TCPChannel - Channel -  RegisterableChannel
 * UDPChannel - Channel -  RegisterableChannel
 * DatagramChannel  -       RegisterableChannel
 * </pre>
 * 
 * @author Dean Hiller
 */
public interface Channel extends RegisterableChannel {

	/**
	 * Connects and registers a DataListener that will be notified of all incoming data. 
     * 
     * NOTE: We do not have a CompletableFuture read() as that can cause result in new gen objects that should
     * never end up in old gen being pulled to old gen.  You can backpressure incoming data using unregisterForReads()
     * and registerForReads() such that data is not read from the socket anymore if you desire on that specific 
     * channel
     * 
     * ie. the Following scenario
     * 1. you read() and then add your listener.  The Future has been created
     * 2. no data comes in for a bit for this read
     * 3. the Future moves to old gen
     * 4. Finally, data comes in and invokes the Future so Future.complete(xxx) is called
     * 5. Now, since the Future is in old gen, it will pull xxx and everything else into old gen along with
     *    it which you should not do causing a very hard to figure out memory issue
     *     
	 * @param addr The address to connect to
	 * @param listener Once connected, this is the listener that will start receiving data
	 * @return
	 */
	public CompletableFuture<Void> connect(SocketAddress addr, DataListener listener);
	public CompletableFuture<Void> write(ByteBuffer b);
	public CompletableFuture<Void> close();
    
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
	
	public boolean isSslChannel();

	/**
	 * Returns null if UDP AND not known yet(has to wait for bind or connect call first)
	 */
	public Boolean isServerSide();
}
