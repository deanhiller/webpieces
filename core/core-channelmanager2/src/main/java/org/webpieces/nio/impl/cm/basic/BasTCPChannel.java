package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.BackpressureConfig;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.jdk.JdkSelect;
import org.webpieces.nio.api.jdk.JdkSocketChannel;
import org.webpieces.util.exceptions.NioClosedChannelException;
import org.webpieces.util.exceptions.NioException;



/**
 * @author Dean Hiller
 */
class BasTCPChannel extends BasChannelImpl implements TCPChannel {

	private static final Logger apiLog = LoggerFactory.getLogger(TCPChannel.class);
	private static final Logger log = LoggerFactory.getLogger(BasTCPChannel.class);
	protected org.webpieces.nio.api.jdk.JdkSocketChannel channel;
	private boolean isClosed;
		    
	public BasTCPChannel(
			String id, JdkSelect selector, SelectorManager2 selMgr, KeyProcessor router, BufferPool pool, BackpressureConfig config
	) {
		super(id, selMgr, router, pool, config);
		this.channelState = ChannelState.NOT_STARTED;

		try {
			channel = selector.open();
			channel.configureBlocking(false);
		} catch(IOException e) {
			throw new NioException(e);
		}
	}
    
	
	/**
	 * Only used from TCPServerChannel.accept().  please keep it that way. thanks.
	 * @param newChan
	 * @param pool 
	 * @param config 
	 * @param executor 
	 */
	public BasTCPChannel(
			String id, JdkSocketChannel newChan, SocketAddress remoteAddr, SelectorManager2 selMgr, KeyProcessor router, BufferPool pool, BackpressureConfig config
	) {
		super(id, selMgr, router, pool, config);
		if(newChan.isBlocking())
			throw new IllegalArgumentException(this+"TCPChannels can only be non-blocking socketChannels");
		channel = newChan;
		if(channel == null)
			throw new IllegalStateException(this+"BasTCPChannel cannot have a null channel");
		
		this.channelState = ChannelState.CONNECTED;
		setConnectingTo(remoteAddr);
	}
	
    protected void bindImpl2(SocketAddress address) throws IOException {
        channel.bind(address);
    }
    
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.SocketChannel#isBound()
	 */
	public boolean isBound() {
		return channel.isBound();
	}
	
	protected boolean isOpen() {
		return channel.isOpen();
	}
	
	protected int writeImpl(ByteBuffer b) {
		try {
			return channel.write(b);
		} catch (IOException e) {
			if(e.getMessage() != null && e.getMessage().equals("Broken pipe")) {
				isClosed = true; //special flag as jdk channel.isClosed in streaming can happen a FULL 1-2 seconds after this due to NIC buffer backup
				throw new NioClosedChannelException(this+"Remote end must have disconnected: Broken Pipe", e);
			}
			throw new NioException(e);
		}
	}
	
	public int readImpl(ByteBuffer b) {
		if(b == null)
			throw new IllegalArgumentException(this+"Cannot use a null ByteBuffer");

		//special code, read information in close() method
		if(isClosed())
			return -1;
		
		try {
			return channel.read(b);
		} catch (IOException e) {
			throw new NioException(e);
		}
	}

    
	/**
     * @see org.webpieces.nio.impl.cm.basic.BasChannelImpl#closeImpl()
     */
    @Override
    protected void closeImpl() throws IOException {
        channel.close();
    }

    public boolean isClosed() {
    	if(isClosed)
    		return true;//special case where we know before the jdk and can discard nic buffer data
		return channel.isClosed();
	}

	public boolean isConnected() {
		return channel.isConnected();
	}

	protected CompletableFuture<Channel> connectImpl(SocketAddress addr) {
		try {
			return connectImpl2(addr);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}
	
	private CompletableFuture<Channel> connectImpl2(SocketAddress addr) throws IOException, InterruptedException {
		CompletableFuture<Channel> future = new CompletableFuture<>();

		if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.connect-addr="+addr);
		try {
			boolean connected = channel.connect(addr);
			if(log.isTraceEnabled())
				log.trace(this+"connected status="+connected);
	
			setConnectingTo(addr);
			if(connected) {
				try {
					future.complete(this);
				} catch(Throwable e) {
					log.error(this+"Exception occurred", e);
				}
			} else {
				return selMgr.registerChannelForConnect(this);
			}
		} catch(Throwable t) {
			log.error("connecting failed");
			future.completeExceptionally(t);
		}
		return future;
	}
	
	public boolean isBlocking() {
		return channel.isBlocking();
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.NetSocketChannel#setReuseAddress(boolean)
	 */
	public void setReuseAddress(boolean b) {
		try {
			channel.setReuseAddress(b);
		} catch (SocketException e) {
			throw new NioException(e);
		}
	}

	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.ClientSocketChannel#getRemoteAddress()
	 */
	public InetSocketAddress getRemoteAddress() {
		InetAddress addr = channel.getInetAddress();
		int port = channel.getPort();
		return new InetSocketAddress(addr, port);
	}
    
    /**
     */
    public InetSocketAddress getLocalAddress()
    {
        InetAddress addr = channel.getLocalAddress();
        int port = channel.getLocalPort();
        
        return new InetSocketAddress(addr, port);
    }


    public void finishConnect() throws IOException {
    	try {
    		channel.finishConnect();
    	} catch(ConnectException e) {
    		ConnectException exc = new ConnectException(this+"could not connect to="+isConnectingTo);
    		exc.initCause(e);
    		throw exc;
    		
    	}
	}
    
    public void setKeepAlive(boolean b) {
    	try {
			channel.setKeepAlive(b);
		} catch (SocketException e) {
			throw new NioException(e);
		}
    }


	public boolean getKeepAlive() {
		try {
			return channel.getKeepAlive();
		} catch (SocketException e) {
			throw new NioException(e);
		}
	}
	
	public int getSoTimeout() {
		try {
			return channel.getSoTimeout();
		} catch (SocketException e) {
			throw new NioException(e);
		}
	}


	@Override
	public boolean isSslChannel() {
		return false;
	}


	@Override
	protected SelectionKey keyFor() {
		return channel.keyFor();
	}


	@Override
	protected SelectionKey register(int allOps, ChannelInfo struct) {
		try {
			return channel.register(allOps, struct);
		} catch (ClosedChannelException e) {
			throw new NioClosedChannelException(this+"On registering, we received closedChannel(did remote end or local end close the socket", e);
		}
	}


}
