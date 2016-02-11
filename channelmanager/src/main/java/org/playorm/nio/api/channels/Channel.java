package org.playorm.nio.api.channels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.handlers.FutureOperation;
import org.playorm.nio.api.handlers.OperationCallback;
import org.playorm.nio.api.libs.ChannelSession;


/**
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
    
    /**
     * Use these two lines of code instead
     * 
     * FutureListener future = channel.write(b);
     * future.wait();
     * 
     * @param b
     */
	@Deprecated
    public int oldWrite(ByteBuffer b);
    
    /**
     * Use these two lines of code instead
     * FutureListener future = channel.write(b);
     * future.setSingleCallback(h); //callback called immediate if write happened between this line and the last line!!
     * or the callback is called later after the write occurs.
     * 
     * @param b
     * @param h
     * @throws IOException 
     * @throws InterruptedException 
     */
	@Deprecated
    public void oldWrite(ByteBuffer b, OperationCallback h);
    
    /**
     * This is synchronous/blocking for TCP and therefore not too scalable.  Use at
     * your own risk.  We advise using the TCPChannel.connect method instead.
     * For UDP, it is not blocking.
     * 
     * @param addr
     */
    @Deprecated
    public void oldConnect(SocketAddress addr); 
    
    /**
     * Asynchronous close where the WriteCloseHandler will be notified once
     * the close is completed.
     * 
     * @param cb The callback that is notified of the completion or failure of the write.
     */
    @Deprecated
    public void oldClose(OperationCallback cb);
    
	/**
	 * Closes and unregisters the channel if registered from the ChannelManager
	 */
	@Deprecated
	public void oldClose();
}
