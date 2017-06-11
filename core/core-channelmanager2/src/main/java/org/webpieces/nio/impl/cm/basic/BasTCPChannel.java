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

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.jdk.JdkSelect;
import org.webpieces.nio.api.jdk.JdkSocketChannel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;



/**
 * @author Dean Hiller
 */
class BasTCPChannel extends BasChannelImpl implements TCPChannel {

	private static final Logger apiLog = LoggerFactory.getLogger(TCPChannel.class);
	private static final Logger log = LoggerFactory.getLogger(BasTCPChannel.class);
	protected org.webpieces.nio.api.jdk.JdkSocketChannel channel;
		    
	public BasTCPChannel(IdObject id, JdkSelect selector, SelectorManager2 selMgr, KeyProcessor router, BufferPool pool) {
		super(id, selMgr, router, pool);
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
	 * @param executor 
	 */
	public BasTCPChannel(IdObject id, JdkSocketChannel newChan, SocketAddress remoteAddr, SelectorManager2 selMgr, KeyProcessor router, BufferPool pool) {
		super(id, selMgr, router, pool);
		if(newChan.isBlocking())
			throw new IllegalArgumentException(this+"TCPChannels can only be non-blocking socketChannels");
		channel = newChan;
		isConnected = true;
		setConnecting(remoteAddr);
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

		apiLog.trace(()->this+"Basic.connect-addr="+addr);
		try {
			boolean connected = channel.connect(addr);
			log.trace(()->this+"connected status="+connected);
	
			setConnecting(addr);
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
    		ConnectException exc = new ConnectException("could not connect to="+isConnectingTo);
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
	protected SelectionKey register(int allOps, WrapperAndListener struct) {
		try {
			return channel.register(allOps, struct);
		} catch (ClosedChannelException e) {
			throw new NioClosedChannelException("On registering, we received closedChannel(did remote end or local end close the socket", e);
		}
	}


	@Override
	protected void resetRegisteredOperations(int ops) {
		channel.resetRegisteredOperations(ops);
	}

}
