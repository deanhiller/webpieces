package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.testutil.chanapi.ChannelsFactory;



/**
 * @author Dean Hiller
 */
class BasTCPServerChannel extends RegisterableChannelImpl implements TCPServerChannel {

	private static final Logger log = LoggerFactory.getLogger(BasTCPServerChannel.class);
	private final ServerSocketChannel channel;
    private final ChannelsFactory channelFactory;
	private final ConnectionListener connectionListener;
	private final DataListener dataListener;
	
	private int i = 0;

	
	public BasTCPServerChannel(IdObject id, ChannelsFactory c, SelectorManager2 selMgr, ConnectionListener listener, DataListener dataListener) {
		super(id, selMgr);
		this.connectionListener = listener;
		this.dataListener = dataListener;
        this.channelFactory = c;
        try {
        	channel = ServerSocketChannel.open();
        	channel.configureBlocking(false);
        } catch(IOException e) {
        	throw new NioException(e);
        }
	}
	
	public int getSession() {
		return i++;
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.TCPServerChannel#accept()
	 */
	public void accept(int newSocketNum) throws IOException {
		try {
			//special code...see information in close() method
			if(isClosed())
				return;
			
			SocketChannel newChan = channel.accept();
			if(newChan == null)
				return;
			newChan.configureBlocking(false);
            
            org.webpieces.nio.api.testutil.chanapi.SocketChannel proxyChan = channelFactory.open(newChan);
		
			IdObject obj = new IdObject(getIdObject(), newSocketNum);
			BasTCPChannel tcpChan = new BasTCPChannel(obj, proxyChan, getSelectorManager(), dataListener);
			if(log.isTraceEnabled())
				log.trace(tcpChan+"Accepted new incoming connection");
			connectionListener.connected(tcpChan);
			
			tcpChan.registerForReads();
			
		} catch(Throwable e) {
			log.warn(this+"Failed to connect", e);
			connectionListener.failed(this, e);
		}
	}
	
	public void registerForReads(DataListener listener) throws IOException, InterruptedException {
		throw new UnsupportedOperationException("TCPServerChannel's can't read, they can only accept incoming connections");
	}
	
	public void registerServerSocketChannel(ConnectionListener cb)  {
		if(!isBound())
			throw new IllegalArgumentException("Only bound sockets can be registered or selector doesn't work");

		try {
			getSelectorManager().registerServerSocketChannel(this, cb);
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		}
	}
	
	public void bind(SocketAddress srvrAddr) {
		try {
			bindImpl(srvrAddr);
			
			registerServerSocketChannel(connectionListener);
		} catch (IOException e) {
			throw new NioException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.TCPServerChannel#bind(java.net.SocketAddress)
	 */
	private void bindImpl(SocketAddress srvrAddr) throws IOException {
		try {
			channel.socket().bind(srvrAddr);
		} catch(BindException e) {
			BindException ee = new BindException("bind exception on addr="+srvrAddr);
			ee.initCause(e);
			throw ee;
		}
	}
	
	public boolean isBound() {
		return channel.socket().isBound();
	}
	
	public void closeServerChannel() {
		//socket.close was resulting in following exception on polling thread.
		//To fix this, we put mechanisms in place to look if this channel
		//was closed or not on the call to accept method
		//
		//INFO: [[client]][ClientChannel] READ 0 bytes(this is strange)
	    //Feb 19, 2006 6:01:22 AM biz.xsoftware.impl.nio.cm.basic.TCPServerChannelImpl accept
	    //WARNING: [[server]][TCPServerChannel] Failed to connect
	    //java.nio.channels.ClosedChannelException
		//at sun.nio.ch.ServerSocketChannelImpl.accept(ServerSocketChannelImpl.java:130)
		//at biz.xsoftware.impl.nio.cm.basic.TCPServerChannelImpl.accept(TCPServerChannelImpl.java:61)
		//at biz.xsoftware.impl.nio.cm.basic.Helper.acceptSocket(Helper.java:109)
		//at biz.xsoftware.impl.nio.cm.basic.Helper.processKey(Helper.java:82)
		//at biz.xsoftware.impl.nio.cm.basic.Helper.processKeys(Helper.java:47)
		//at biz.xsoftware.impl.nio.cm.basic.SelectorManager2.runLoop(SelectorManager2.java:305)
		//at biz.xsoftware.impl.nio.cm.basic.SelectorManager2$PollingThread.run(SelectorManager2.java:267)
		try {
			channel.socket().close();
			channel.close();
			super.wakeupSelector();			
        } catch(Exception e) {
            log.warn(this+"Exception closing channel", e);
        }
	}
	
	public boolean isClosed() {
		return channel.socket().isClosed();
	}
	
	/**
	 */
	public SelectableChannel getRealChannel() {
		return channel;
	}
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.RegisterableChannel#isBlocking()
	 */
	public boolean isBlocking() {
		return channel.isBlocking();
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.RegisterableChannel#setReuseAddress(boolean)
	 */
	public void setReuseAddress(boolean b) {
		try {
			channel.socket().setReuseAddress(b);
		} catch (SocketException e) {
			throw new NioException(e);
		}
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.SocketSuperclass#getLocalAddress()
	 */
	public InetSocketAddress getLocalAddress() {
		InetAddress addr = channel.socket().getInetAddress();
		int port = channel.socket().getLocalPort();
		return new InetSocketAddress(addr, port);
	}	
}
