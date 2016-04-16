package org.webpieces.nio.impl.util;

import javax.net.ssl.SSLEngine;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.TCPChannel;
import org.webpieces.nio.api.handlers.FutureOperation;


public abstract class UtilTCPChannel extends UtilChannel {
	
	public UtilTCPChannel(Channel realChannel) {
		super(realChannel);
	}

	@Override
	protected TCPChannel getRealChannel() {
		return (TCPChannel) super.getRealChannel();
	}

	public void oldClose() {
		TCPChannel realChannel = getRealChannel();
		realChannel.oldClose();
	}
	
	public boolean getKeepAlive() {
		TCPChannel realChannel = getRealChannel();
		return realChannel.getKeepAlive();
	}

	public void setKeepAlive(boolean b) {
		TCPChannel realChannel = getRealChannel();
		realChannel.setKeepAlive(b);
	}
	
	public FutureOperation openSSL(SSLEngine engine) {
		TCPChannel realChannel = getRealChannel();
		return realChannel.openSSL(engine);
	}

	public FutureOperation closeSSL() {
		TCPChannel realChannel = getRealChannel();
		return realChannel.closeSSL();
	}
	
	public boolean isInSslMode() {
		TCPChannel realChannel = getRealChannel();
		return realChannel.isInSslMode();
	}
}
