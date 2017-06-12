package org.webpieces.nio.impl.jdk;

import java.io.IOException;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;



/**
 */
public class JdkSocketChannelImpl implements org.webpieces.nio.api.jdk.JdkSocketChannel{

    private SocketChannel channel;
	private Selector selector;

    public JdkSocketChannelImpl(SocketChannel c, Selector selector) {
        channel = c;
		this.selector = selector;
    }
    
    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#configureBlocking(boolean)
     */
    public void configureBlocking(boolean b) throws IOException {
        channel.configureBlocking(b);
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#isBlocking()
     */
    public boolean isBlocking() {
        return channel.isBlocking();
    }

    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#bind(java.net.SocketAddress)
     */
    public void bind(SocketAddress addr) throws IOException {
        channel.socket().bind(addr);
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#isBound()
     */
    public boolean isBound() {
        return channel.socket().isBound();
    }

    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#write(java.nio.ByteBuffer)
     */
    public int write(ByteBuffer b) throws IOException {
        return channel.write(b);
    }

    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#read(java.nio.ByteBuffer)
     */
    public int read(ByteBuffer b) throws IOException {
        return channel.read(b);
    }

    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#close()
     */
    public void close() throws IOException {
        channel.socket().close();
        channel.close();        
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#isClosed()
     */
    public boolean isClosed()
    {
        return channel.socket().isClosed();
    }

	@Override
	public boolean isOpen() {
		return getSelectableChannel().isOpen();
	}
	
    /**
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#isConnected()
     */
    public boolean isConnected()
    {
        return channel.socket().isConnected();
    }

    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#connect(java.net.SocketAddress)
     */
    public boolean connect(SocketAddress addr) throws IOException
    {
        return channel.connect(addr);
    }

    /**
     * @throws SocketException 
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#setReuseAddress(boolean)
     */
    public void setReuseAddress(boolean b) throws SocketException {
        channel.socket().setReuseAddress(b);
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#getInetAddress()
     */
    public InetAddress getInetAddress() {
        return channel.socket().getInetAddress();
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#getPort()
     */
    public int getPort() {
        return channel.socket().getPort();
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#getLocalAddress()
     */
    public InetAddress getLocalAddress() {
        return channel.socket().getLocalAddress();
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#getLocalPort()
     */
    public int getLocalPort() {
        return channel.socket().getLocalPort();
    }

    /**
     * @throws IOException 
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#finishConnect()
     */
    public void finishConnect() throws IOException {
        channel.finishConnect();
    }

    /**
     * @see org.webpieces.nio.api.jdk.JdkSocketChannel#getSelectableChannel()
     */
    private java.nio.channels.SelectableChannel getSelectableChannel()
    {
        return channel;
    }

	public boolean getKeepAlive() throws SocketException {
		return channel.socket().getKeepAlive();
	}

	public void setKeepAlive(boolean b) throws SocketException {
		channel.socket().setKeepAlive(b);
	}

	@Override
	public int getSoTimeout() throws SocketException {
		return channel.socket().getSoTimeout();
	}

	@Override
	public SelectionKey register(int allOps, Object struct) throws ClosedChannelException {
    	if(struct == null)
    		throw new IllegalArgumentException("struct cannot be null");
    	
		return getSelectableChannel().register(selector, allOps, struct);
	}

	@Override
	public SelectionKey keyFor() {
        return getSelectableChannel().keyFor(selector);
	}

	@Override
	public SocketAddress getRemoteAddress() throws IOException {
		return channel.getRemoteAddress();
	}

}
