package org.playorm.nio.impl.cm.basic;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectableChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import org.playorm.nio.api.channels.NioException;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.handlers.FutureOperation;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.testutil.chanapi.ChannelsFactory;
import org.playorm.nio.api.testutil.chanapi.SocketChannel;
import org.playorm.nio.impl.util.UtilWaitForConnect;



/**
 * @author Dean Hiller
 */
class BasTCPChannel extends BasChannelImpl implements TCPChannel {

	private static final Logger apiLog = Logger.getLogger(TCPChannel.class.getName());
	private static final Logger log = Logger.getLogger(BasTCPChannel.class.getName());
	private org.playorm.nio.api.testutil.chanapi.SocketChannel channel;
		    
	public BasTCPChannel(IdObject id, ChannelsFactory factory, BufferFactory bufFactory, 
                          SelectorManager2 selMgr) throws IOException {
		super(id, bufFactory, selMgr); 
		channel = factory.open();
		channel.configureBlocking(false);
	}
    
	
	/**
	 * Only used from TCPServerChannel.accept().  please keep it that way. thanks.
	 * @param newChan
	 */
	public BasTCPChannel(IdObject id, BufferFactory bufFactory, 
                          SocketChannel newChan, SelectorManager2 selMgr) {
		super(id, bufFactory, selMgr);
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
	

	
	/* Should probably synchronize this? as the after if(isClosed()) is called,
	 * the channel may close on another thread resulting in a ClosedChannelException....
	 * or we could allow these threw and wrap them with a probably not a bad thing
	 * exception...you just got unlucky!!!!
	 * 
	 * @see api.biz.xsoftware.nio.SocketChannel#write(java.nio.ByteBuffer)
	 */
	public int unusedOldWrite(ByteBuffer b) throws IOException {				
		int remain = b.remaining();
		int i = 0;
		while(b.hasRemaining()) {
			i++;
			int result = channel.write(b);
			
			//TODO: my performance tests showed when in lightweight apps, the performance of
			//this channelmanager is so good, it floods the nic's outgoing buffer and can't 
			//write to it....This sleep allows it to clean out it's buffer but should really 
			//be a register for write so the selector can notify us when we can write again....
			if(i > 5) {
				log.warning(this+"Having trouble writing data out.  result="+result+" b="+b);
				try {
					Thread.sleep(50*i); //this is a backoff, so it keeps backing off more and more until 500 ms
				} catch (InterruptedException e) {
					log.log(Level.WARNING, this+"exception", e);
				}
			} else if(i > 10) 
				throw new RuntimeException(this+"Bug, tried to write 1000 times and could not");
		}
		assert b.remaining() == 0 : this+"Did not write out all bytes";

		return remain;
	}
	
	protected int writeImpl(ByteBuffer b) throws IOException {
		return channel.write(b);
	}
	
	public int readImpl(ByteBuffer b) throws IOException {
		if(b == null)
			throw new IllegalArgumentException(this+"Cannot use a null ByteBuffer");

		//special code, read information in close() method
		if(isClosed())
			return -1;
		return channel.read(b);
	}

    
	/**
     * @see org.playorm.nio.impl.cm.basic.BasChannelImpl#closeImpl()
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

	public void oldConnect(SocketAddress addr) {
		try {
			oldConnectImpl(addr);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.TCPChannel#connect(java.net.SocketAddress)
	 */
	public void oldConnectImpl(SocketAddress addr) throws IOException, InterruptedException {
		if(isBlocking()) {
			channel.connect(addr);
		} else {
			UtilWaitForConnect connect = new UtilWaitForConnect();				
			oldConnect(addr, connect);
			connect.waitForConnect();
		}
	}

	@Override
	public FutureOperation connect(SocketAddress addr) {
		try {
			return connectImpl(addr);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}

	private FutureOperation connectImpl(SocketAddress addr) throws IOException, InterruptedException {
		FutureConnectImpl future = new FutureConnectImpl();

		if(apiLog.isLoggable(Level.FINE))
			apiLog.fine(this+"Basic.connect-addr="+addr);
		
		boolean connected = channel.connect(addr);
		if(log.isLoggable(Level.FINER))
			log.finer(this+"connected status="+connected);

		setConnecting(true);
		if(connected) {
			try {
				future.connected(this);
			} catch(Throwable e) {
				log.log(Level.WARNING, this+"Exception occurred", e);
			}
		} else {
			getSelectorManager().registerChannelForConnect(this, future);
		}
		return future;
	}
	
	public void oldConnect(SocketAddress addr, ConnectionCallback c){
		try {
			oldConnectImpl(addr, c);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}
	public void oldConnectImpl(SocketAddress addr, ConnectionCallback c) throws IOException, InterruptedException {
		if(c == null)
			throw new IllegalArgumentException(this+"ConnectCallback cannot be null");

		if(apiLog.isLoggable(Level.FINE))
			apiLog.fine(this+"Basic.connect-addr="+addr);
		
		boolean connected = channel.connect(addr);
		if(log.isLoggable(Level.FINER))
			log.finer(this+"connected status="+connected);

		setConnecting(true);
		if(connected) {
			try {
				c.connected(this);
			} catch(Throwable e) {
				log.log(Level.WARNING, this+"Exception occurred", e);
			}
		} else {
			getSelectorManager().registerChannelForConnect(this, c);	
		}
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

	@Override
	public FutureOperation openSSL(SSLEngine engine) {
		throw new UnsupportedOperationException("should never be called");
	}


	@Override
	public FutureOperation closeSSL() {
		throw new UnsupportedOperationException("should never be called");
	}


	@Override
	public boolean isInSslMode() {
		throw new UnsupportedOperationException("should never be called");
	}
    
    
}
