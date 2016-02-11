package org.playorm.nio.api.testutil.chanapi;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;


/**
 */
public interface SocketChannel
{

    /**
     * @param b
     * @throws IOException 
     */
    void configureBlocking(boolean b) throws IOException;

    /**
     * @return true if this channel is blocking, and false otherwise.
     */
    boolean isBlocking();

    /**
     * @param addr
     * @throws IOException 
     */
    void bind(SocketAddress addr) throws IOException;

    /**
     * @return true if this channel is bound, and false otherwise.
     */
    boolean isBound();

    /**
     * @param b
     * @return the number of bytes written
     * @throws IOException 
     */
    int write(ByteBuffer b) throws IOException;

    /**
     * @param b
     * @return the number of bytes read
     * @throws IOException 
     */
    int read(ByteBuffer b) throws IOException;

    /**
     * @throws IOException 
     * 
     */
    void close() throws IOException;

    /**
     * @return true if this channel is closed, and false otherwise.
     */
    boolean isClosed();

    /**
     * @return true if this channel is connected, and false otherwise.
     */
    boolean isConnected();

    /**
     * @param addr
     * @throws IOException 
     */
    boolean connect(SocketAddress addr) throws IOException;

    /**
     * @param b
     * @throws SocketException 
     */
    void setReuseAddress(boolean b) throws SocketException;

    /**
     * @return the remote InetAddress
     */
    InetAddress getInetAddress();

    /**
     * @return the remote port
     */
    int getPort();

    /**
     * @return the local InetAddress
     */
    InetAddress getLocalAddress();

    /**
     * @return the local port
     */
    int getLocalPort();

    /**
     * @throws IOException 
     * 
     */
    void finishConnect() throws IOException;

    /**
     * @return the SelectableChannel for this Channel
     */
    SelectableChannel getSelectableChannel();

	void setKeepAlive(boolean b) throws SocketException;

	boolean getKeepAlive() throws SocketException;



    

}
