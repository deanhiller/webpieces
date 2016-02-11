package org.playorm.nio.impl.cm.routing;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.channels.TCPServerChannel;
import org.playorm.nio.api.handlers.ConnectionListener;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.ChannelsRunnable;


class ThdProxyAcceptCb implements ConnectionListener {

	private static final Logger log = Logger.getLogger(ThdProxyAcceptCb.class.getName());
	
	private TCPServerChannel svrChannel;
	private ConnectionListener cb;
	private SpecialRoutingExecutor svc;
	private BufferFactory bufFactory;
	
	public ThdProxyAcceptCb(TCPServerChannel svrChannel, ConnectionListener cb, SpecialRoutingExecutor svc2, BufferFactory bufFactory) {
		this.svrChannel = svrChannel;
		this.cb = cb;
		this.svc = svc2;
		this.bufFactory = bufFactory;
	}
	
	public void connected(final Channel channel) throws IOException {
		ChannelsRunnable r = new ChannelsRunnable() {
			public void run() {
				try {
					TCPChannel newChannel = new ThdTCPChannel((TCPChannel) channel, svc, bufFactory);
					cb.connected(newChannel);
				} catch (Exception e) {
					log.log(Level.WARNING, channel+"Exception", e);
				}				
			}
			public RegisterableChannel getChannel() {
				return svrChannel;
			}			
		};
		svc.execute(channel, r);			
	}

	public void failed(RegisterableChannel channel, final Throwable e) {
        ChannelsRunnable r = new ChannelsRunnable() {
			public void run() {
				try {
					cb.failed(svrChannel, e);
				} catch (Exception e) {
					log.log(Level.WARNING, svrChannel+"Exception", e);
				}				
			}
			public RegisterableChannel getChannel() {
				return svrChannel;
			}			
		};
		svc.execute(null, r);			
	}
}
