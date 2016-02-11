package org.playorm.nio.impl.cm.routing;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.channels.TCPChannel;
import org.playorm.nio.api.deprecated.ConnectionCallback;
import org.playorm.nio.api.handlers.ConnectionListener;
import org.playorm.nio.api.libs.ChannelsRunnable;


class ThdProxyConnectCb implements ConnectionCallback {

	private static final Logger log = Logger.getLogger(ThdProxyConnectCb.class.getName());
	
	private TCPChannel channel;
	private ConnectionListener cb;
	private SpecialRoutingExecutor svc;

	public ThdProxyConnectCb(TCPChannel channel, ConnectionListener cb, SpecialRoutingExecutor svc2) {
		this.channel = channel;
		this.cb = cb;
		this.svc = svc2;
	}
	
	public void connected(Channel realChannel) throws IOException {
        ChannelsRunnable r = new ChannelsRunnable() {
			public void run() {
				try {
					cb.connected(channel);
				} catch (Exception e) {
					log.log(Level.WARNING, channel+"Exception", e);
				}				
			}
			public RegisterableChannel getChannel() {
				return channel;
			}
		};
		svc.execute(realChannel, r);
	}

	public void failed(RegisterableChannel realChannel, final Throwable e) {
        ChannelsRunnable r = new ChannelsRunnable() {
			public void run() {
				try {
					cb.failed(channel, e);
				} catch (Exception e) {
					log.log(Level.WARNING, channel+"Exception", e);
				}				
			}
			public RegisterableChannel getChannel() {
				return channel;
			}
		};
		svc.execute(null, r);		
	}
}
