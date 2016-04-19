package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.testutil.chanapi.ChannelsFactory;
import org.webpieces.nio.api.testutil.chanapi.SocketChannel;
import org.webpieces.util.futures.Future;
import org.webpieces.util.futures.PromiseImpl;



/**
 * @author Dean Hiller
 */
class BasTCPChannel extends BasChannelImpl implements TCPChannel {

	private static final Logger apiLog = LoggerFactory.getLogger(TCPChannel.class);
	private static final Logger log = LoggerFactory.getLogger(BasTCPChannel.class);
	private org.webpieces.nio.api.testutil.chanapi.SocketChannel channel;
		    
	public BasTCPChannel(IdObject id, ChannelsFactory factory, SelectorManager2 selMgr) {
		super(id, selMgr);
		try {
			channel = factory.open();
			channel.configureBlocking(false);
		} catch(IOException e) {
			throw new NioException(e);
		}
	}
    
	
	/**
	 * Only used from TCPServerChannel.accept().  please keep it that way. thanks.
	 * @param newChan
	 */
	public BasTCPChannel(IdObject id, SocketChannel newChan, SelectorManager2 selMgr) {
		super(id, selMgr);
		if(newChan.isBlocking())
			throw new IllegalArgumentException(this+"TCPChannels can only be non-blocking socketChannels");
		channel = newChan;
		setConnecting(true);
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

	@Override
	public Future<Channel, FailureInfo> connect(SocketAddress addr) {
		try {
			return connectImpl(addr);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}

	private Future<Channel, FailureInfo> connectImpl(SocketAddress addr) throws IOException, InterruptedException {
		PromiseImpl<Channel, FailureInfo> future = new PromiseImpl<>();

		if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.connect-addr="+addr);
		
		boolean connected = channel.connect(addr);
		if(log.isTraceEnabled())
			log.trace(this+"connected status="+connected);

		setConnecting(true);
		if(connected) {
			try {
				future.setResult(this);
			} catch(Throwable e) {
				log.warn(this+"Exception occurred", e);
			}
		} else {
			getSelectorManager().registerChannelForConnect(this, future);
		}
		return future;
	}
	
	public boolean isBlocking() {
		return channel.isBlocking();
	}

	/* (non-Javadoc)
	 * @see biz.xsoftware.nio.RegisterableChannelImpl#getRealChannel()
	 */
	public SelectableChannel getRealChannel() {
		return channel.getSelectableChannel();
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
		channel.finishConnect();
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
    
}
