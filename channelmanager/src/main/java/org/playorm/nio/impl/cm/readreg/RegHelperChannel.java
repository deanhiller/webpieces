package org.playorm.nio.impl.cm.readreg;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.NioException;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.impl.util.UtilChannel;


public class RegHelperChannel extends UtilChannel implements Channel {

	private static final Logger apiLog = Logger.getLogger(Channel.class.getName());
	protected DataListener cachedListener;
	protected boolean isRegistered = false;
	public RegHelperChannel(Channel realChannel) {
		super(realChannel);
	}

	@Override
	public synchronized void registerForReads(DataListener listener) {
		if(apiLog.isLoggable(Level.FINE))
			apiLog.fine(this+"RegRead.registerForReads called");
		cachedListener = listener;
		if(!isConnected()) {
			return;
		}
	
		getRealChannel().registerForReads(listener);
		isRegistered = true;
	}
	
	@Override
	public synchronized void unregisterForReads() {
		if(apiLog.isLoggable(Level.FINE))
			apiLog.fine(this+"RegRead.unregisterForReads called");
		cachedListener = null;
		if(!isConnected()) {
			return;
		}
		getRealChannel().unregisterForReads();
		isRegistered = false;
	}

	@Override
	public void oldConnect(SocketAddress addr) {
		try {
			oldConnectImpl(addr);
		} catch (IOException e) {
			throw new NioException(e);
		}
	}
	
	private synchronized void oldConnectImpl(SocketAddress addr) throws IOException {
		if(apiLog.isLoggable(Level.FINE))
			apiLog.fine(this+"RegRead.connect called-addr="+addr);
		
		super.oldConnect(addr);
		if(cachedListener != null) {
			getRealChannel().registerForReads(cachedListener);
			isRegistered = true;
		}
	}
		
}
