package org.webpieces.nio.impl.cm.basic.udp;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectableChannel;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.data.api.BufferPool;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.UDPChannel;
import org.webpieces.nio.api.exceptions.NioException;
import org.webpieces.nio.api.exceptions.NioPortUnreachableException;
import org.webpieces.nio.impl.cm.basic.BasChannelImpl;
import org.webpieces.nio.impl.cm.basic.IdObject;
import org.webpieces.nio.impl.cm.basic.SelectorManager2;


public class UDPChannelImpl extends BasChannelImpl implements UDPChannel {

	private static final Logger log = LoggerFactory.getLogger(UDPChannel.class);
	private static final Logger apiLog = LoggerFactory.getLogger(UDPChannel.class);
	private DatagramChannel channel;
	private boolean isConnected = false;
    private Calendar expires;
    
	public UDPChannelImpl(IdObject id, SelectorManager2 selMgr, BufferPool pool) {
		super(id, selMgr, pool);
		try {
			channel = DatagramChannel.open();
			channel.configureBlocking(false);
	        channel.socket().setReuseAddress(true);
		} catch(IOException e) {
			throw new NioException(e);
		}
	}

	public void bindImpl2(SocketAddress addr) throws IOException {
        channel.socket().bind(addr);
	}
	
	protected synchronized CompletableFuture<Channel> connectImpl(SocketAddress addr) {
		CompletableFuture<Channel> promise = new CompletableFuture<>();
		
		try {
			if(apiLog.isTraceEnabled())
				apiLog.trace(this+"Basic.connect called-addr="+addr);
			
			channel.connect(addr);
			
	        isConnected = true;
	        promise.complete(this);
		} catch(Exception e) {
			promise.completeExceptionally(e);
		}
		
        return promise;
	}
    
    public synchronized void disconnect() {
		if(apiLog.isTraceEnabled())
			apiLog.trace(this+"Basic.disconnect called");
		
		try {
			isConnected = false;        
			channel.disconnect();
		} catch(IOException e) {
			throw new NioException(e);
		}
    }

    public void setReuseAddress(boolean b) {
		throw new UnsupportedOperationException("not implemented yet");
	}

	public boolean isBlocking() {
		return channel.isBlocking();
	}

	public void closeImpl() throws IOException {
		channel.close();
	}

	public boolean isClosed() {
		return channel.socket().isClosed();
	}

	public boolean isBound() {
		return channel.socket().isBound();
	}

	public InetSocketAddress getLocalAddress() {
		InetAddress addr = channel.socket().getLocalAddress();
		int port = channel.socket().getLocalPort();      
		return new InetSocketAddress(addr, port);
	}

	public InetSocketAddress getRemoteAddress() {
		return (InetSocketAddress)channel.socket().getRemoteSocketAddress();
	}
    
	public boolean isConnected() {
		return channel.isConnected();
	}

	@Override
	public SelectableChannel getRealChannel() {
		return channel;
	}
	
	@Override
	public int readImpl(ByteBuffer b) {
		if(b == null)
			throw new IllegalArgumentException("Cannot use a null ByteBuffer");
		else if(!isConnected)
			throw new IllegalStateException("Currently not connected");
		try {
			return channel.read(b);
		} catch(PortUnreachableException e) {
			if(expires != null) {
				//ignore the event if we are not at expires yet
				if(Calendar.getInstance().before(expires)) {
					return 0;
				}
			}

			expires = Calendar.getInstance();
			expires.add(Calendar.SECOND, 10);
			log.error("PortUnreachable.  NOTICE NOTICE:  We will ignore this exc again on this channel for 10 seconds");
			throw new NioPortUnreachableException(e);
		} catch (IOException e) {
			throw new NioException(e);
		}
	}

	@Override
	protected int writeImpl(ByteBuffer b) {
		try {
			return channel.write(b);
		} catch (IOException e) {
			throw new NioException(e);
		}
	}

	@Override
	public boolean isSslChannel() {
		return false;
	}

}
