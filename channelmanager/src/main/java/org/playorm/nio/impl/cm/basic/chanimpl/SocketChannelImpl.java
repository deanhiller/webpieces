package org.playorm.nio.impl.cm.basic.chanimpl;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;



/**
 */
public class SocketChannelImpl implements org.playorm.nio.api.testutil.chanapi.SocketChannel{

    private SocketChannel channel;

    public SocketChannelImpl(SocketChannel c) {
        channel = c;
    }
    
    /**
     * @throws IOException 
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#configureBlocking(boolean)
     */
    public void configureBlocking(boolean b) throws IOException {
        channel.configureBlocking(b);
    }

    /**
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#isBlocking()
     */
    public boolean isBlocking() {
        return channel.isBlocking();
    }

    /**
     * @throws IOException 
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#bind(java.net.SocketAddress)
     */
    public void bind(SocketAddress addr) throws IOException {
        channel.socket().bind(addr);
    }

    /**
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#isBound()
     */
    public boolean isBound() {
        return channel.socket().isBound();
    }

    /**
     * @throws IOException 
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#write(java.nio.ByteBuffer)
     */
    public int write(ByteBuffer b) throws IOException {
        return channel.write(b);
    }

    /**
     * @throws IOException 
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#read(java.nio.ByteBuffer)
     */
    public int read(ByteBuffer b) throws IOException {
        return channel.read(b);
    }

    /**
     * @throws IOException 
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#close()
     */
    public void close() throws IOException {
        channel.socket().close();
        channel.close();        
    }

    /**
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#isClosed()
     */
    public boolean isClosed()
    {
        return channel.socket().isClosed();
    }

    /**
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#isConnected()
     */
    public boolean isConnected()
    {
        return channel.socket().isConnected();
    }

    /**
     * @throws IOException 
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#connect(java.net.SocketAddress)
     */
    public boolean connect(SocketAddress addr) throws IOException
    {
        return channel.connect(addr);
    }

    /**
     * @throws SocketException 
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#setReuseAddress(boolean)
     */
    public void setReuseAddress(boolean b) throws SocketException {
        channel.socket().setReuseAddress(b);
    }

    /**
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#getInetAddress()
     */
    public InetAddress getInetAddress() {
        return channel.socket().getInetAddress();
    }

    /**
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#getPort()
     */
    public int getPort() {
        return channel.socket().getPort();
    }

    /**
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#getLocalAddress()
     */
    public InetAddress getLocalAddress() {
        return channel.socket().getLocalAddress();
    }

    /**
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#getLocalPort()
     */
    public int getLocalPort() {
        return channel.socket().getLocalPort();
    }

    /**
     * @throws IOException 
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#finishConnect()
     */
    public void finishConnect() throws IOException {
        channel.finishConnect();
    }

    /**
     * @see org.playorm.nio.api.testutil.chanapi.SocketChannel#getSelectableChannel()
     */
    public java.nio.channels.SelectableChannel getSelectableChannel()
    {
        return channel;
    }

	public boolean getKeepAlive() throws SocketException {
		return channel.socket().getKeepAlive();
	}

	public void setKeepAlive(boolean b) throws SocketException {
		channel.socket().setKeepAlive(b);
	}

}
