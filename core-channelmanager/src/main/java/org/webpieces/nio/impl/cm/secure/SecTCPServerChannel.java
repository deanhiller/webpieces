package org.webpieces.nio.impl.cm.secure;

import org.webpieces.nio.api.channels.TCPServerChannel;
import org.webpieces.nio.api.handlers.ConnectionListener;
import org.webpieces.nio.api.libs.SSLEngineFactory;
import org.webpieces.nio.impl.util.UtilRegisterable;


class SecTCPServerChannel extends UtilRegisterable implements TCPServerChannel {

	private TCPServerChannel realChannel;
	private SSLEngineFactory sslFactory;
	
	public SecTCPServerChannel(TCPServerChannel c, SSLEngineFactory sslFactory) {
		super(c);
		realChannel = c;
		this.sslFactory = sslFactory;
	}
		
	
	@Override
	protected TCPServerChannel getRealChannel() {
		return (TCPServerChannel) super.getRealChannel();
	}


	public void closeServerChannel() {
		realChannel.closeServerChannel();
	}
	
	public void registerServerSocketChannel(ConnectionListener listener) {
		SecProxyConnectCb proxyList = new SecProxyConnectCb(this, sslFactory, listener);
		realChannel.registerServerSocketChannel(proxyList);
	}
	
}