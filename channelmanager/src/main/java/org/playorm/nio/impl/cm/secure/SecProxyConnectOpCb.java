package org.playorm.nio.impl.cm.secure;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLEngine;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.handlers.OperationCallback;
import org.playorm.nio.api.libs.AsyncSSLEngine;
import org.playorm.nio.api.libs.FactoryCreator;
import org.playorm.nio.api.libs.SSLEngineFactory;


class SecProxyConnectOpCb implements OperationCallback {

	private static final Logger log = Logger.getLogger(SecProxyConnectOpCb.class.getName());
	private static final FactoryCreator CREATOR = FactoryCreator.createFactory(null);
	
	private SecTCPChannel channel;
	private OperationCallback cb;
	private SSLEngineFactory sslFactory;
	private SecTCPServerChannel svrChannel;
	
	//called by TCPChannelImpl.connect method
	public SecProxyConnectOpCb(SecTCPChannel impl, SSLEngineFactory factory, OperationCallback cb) {
		this.channel = impl;
		this.cb = cb;
		this.sslFactory = factory;
	}

	@Override
	public void finished(Channel realChannel) throws IOException {
		if(log.isLoggable(Level.FINE))
			log.fine(realChannel+" Tcp connected, running handshake before fire connect");
		SecTCPChannel secureChannel = channel;
		SSLEngine sslEngine;
		try {
			if(svrChannel != null) {
				sslEngine = sslFactory.createEngineForServerSocket();			
				secureChannel = new SecTCPChannel((TCPChannel) realChannel);
			} else
				sslEngine = sslFactory.createEngineForSocket();
		} catch (GeneralSecurityException e) {
			IOException ioe = new IOException(realChannel+"Security error");
			ioe.initCause(e);
			throw ioe;
		}
		
		SecSSLListener connectProxy = secureChannel.getConnectProxy();

		AsyncSSLEngine handler = CREATOR.createSSLEngine(realChannel, sslEngine, null);
//		AsynchSSLEngine handler = new AsynchSSLEngineImpl(realChannel, sslEngine);
//		AsynchSSLEngine handler = new AsynchSSLEngineSynchronized(realChannel, sslEngine);
//		AsynchSSLEngine handler = new AsynchSSLEngineQueued()
		secureChannel.getReaderProxy().setHandler(handler);
		handler.setListener(secureChannel.getConnectProxy());
		
		connectProxy.setConnectCallback(new ProxyCallback(cb));
		synchronized(secureChannel) {
			if(log.isLoggable(Level.FINEST))
				log.finest(realChannel+" about to register for reads");				
			if(!connectProxy.isClientRegistered()) {
				if(log.isLoggable(Level.FINEST))
					log.finest(realChannel+" register for reads");		
				realChannel.registerForReads(secureChannel.getReaderProxy());
			}
		}

		handler.beginHandshake();
	}

	public void failed(RegisterableChannel channel, Throwable e) {
		if(channel != null)
			cb.failed(channel, e);
		else
			cb.failed(svrChannel, e);
	}

	private static class ProxyCallback implements ConnectionCallback {
		private OperationCallback cb;
		
		public ProxyCallback(OperationCallback cb) {
			this.cb = cb;
		}

		@Override
		public void connected(Channel channel) throws IOException {
			cb.finished(channel);
		}

		@Override
		public void failed(RegisterableChannel channel, Throwable e) {
			cb.failed(channel, e);
		}
	}
}
