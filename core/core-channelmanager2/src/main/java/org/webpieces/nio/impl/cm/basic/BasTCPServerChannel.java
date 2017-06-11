package org.webpieces.nio.impl.cm.basic;

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.exceptions.NioClosedChannelException;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.handlers.ConsumerFunc;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.jdk.JdkSelect;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;



/**
 * @author Dean Hiller
 */
class BasTCPServerChannel extends RegisterableChannelImpl implements TCPServerChannel {

	private static final Logger log = LoggerFactory.getLogger(BasTCPServerChannel.class);
	protected org.webpieces.nio.api.jdk.JdkServerSocketChannel channel;
    private final JdkSelect selector;
	private final ConnectionListener connectionListener;
	private BufferPool pool;	
	private int channelCount = 0;
	private KeyProcessor router;
	
	public BasTCPServerChannel(IdObject id, JdkSelect c, SelectorManager2 selMgr, KeyProcessor router,
			ConnectionListener listener, BufferPool pool) {
		super(id, selMgr);
		this.router = router;
		this.connectionListener = listener;
        this.selector = c;
        this.pool = pool;
        try {
        	channel = c.openServerSocket();
        	channel.configureBlocking(false);
        } catch(IOException e) {
        	throw new NioException(e);
        }
	}
	
	public int getChannelCount() {
		return channelCount++;
	}
	
	/* (non-Javadoc)
	 * @see api.biz.xsoftware.nio.TCPServerChannel#accept()
	 */
	public void accept(int newSocketNum) throws IOException {
		CompletableFuture<Void> future;
		try {
			//special code...see information in close() method
			if(isClosed())
				return;
			
			SocketChannel newChan = channel.accept();
			if(newChan == null)
				return;
			newChan.configureBlocking(false);
            
            org.webpieces.nio.api.jdk.JdkSocketChannel proxyChan = selector.open(newChan);
		
            SocketAddress remoteAddress = newChan.getRemoteAddress();
			IdObject obj = new IdObject(getIdObject(), newSocketNum);
			BasTCPChannel tcpChan = new BasTCPChannel(obj, proxyChan, remoteAddress, selMgr, router, pool);
			log.trace(()->tcpChan+"Accepted new incoming connection");
			CompletableFuture<DataListener> connectFuture = connectionListener.connected(tcpChan, true);
			future = connectFuture.thenCompose(l -> tcpChan.registerForReads(l)).thenApply(c -> null);
			
		} catch(Throwable e) {
			future = new CompletableFuture<Void>();
			future.completeExceptionally(e);
		}
		
		future.exceptionally(t -> {
			log.error(this+"Failed to connect", t);
			connectionListener.failed(this, t);
			return null;
		});
	}
	
	public void registerForReads(DataListener listener) throws IOException, InterruptedException {
		throw new UnsupportedOperationException("TCPServerChannel's can't read, they can only accept incoming connections");
	}
	
	public void registerServerSocketChannel(ConnectionListener cb)  {
		if(!isBound())
			throw new IllegalArgumentException("Only bound sockets can be registered or selector doesn't work");

		try {
			CompletableFuture<Void> future = selMgr.registerServerSocketChannel(this, cb);
			future.get();
		} catch (IOException e) {
			throw new NioException(e);
		} catch (InterruptedException e) {
			throw new NioException(e);
		} catch (ExecutionException e) {
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
            log.error(this+"Exception closing channel", e);
        }
	}
	
	public boolean isClosed() {
		return channel.socket().isClosed();
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
		if(!channel.socket().isBound())
			throw new IllegalStateException("Socket not bound yet.  please bind before calling getLocalAddress");
		InetAddress addr = channel.socket().getInetAddress();
		int port = channel.socket().getLocalPort();
		return new InetSocketAddress(addr, port);
	}

	@Override
	public void configure(ConsumerFunc<ServerSocketChannel> methodToConfigure) {
		try {
			if(methodToConfigure != null)
				methodToConfigure.accept(channel.getRealChannel());
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	public ServerSocketChannel getUnderlyingChannel() {
		return channel.getRealChannel();
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
			throw new NioClosedChannelException("darn checked exceptions", e);
		}
	}

	@Override
	protected void resetRegisteredOperations(int ops) {
		channel.resetRegisteredOperations(ops);
	}	
}
