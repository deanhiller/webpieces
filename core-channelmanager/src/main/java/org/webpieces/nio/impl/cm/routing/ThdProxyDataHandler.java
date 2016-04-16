package org.webpieces.nio.impl.cm.routing;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.handlers.DataListener;
import org.webpieces.nio.api.libs.BufferFactory;
import org.webpieces.nio.api.libs.ChannelsRunnable;


class ThdProxyDataHandler implements DataListener {

	private static final Logger log = Logger.getLogger(ThdProxyDataHandler.class.getName());
	private ThdTCPChannel channel;
	private DataListener handler;
	private SpecialRoutingExecutor svc;

	public ThdProxyDataHandler(ThdTCPChannel channel, DataListener handler, SpecialRoutingExecutor svc2, BufferFactory bufFactory) {
		this.channel = channel;
		this.handler = handler;
		this.svc = svc2;
	}
	
	public void incomingData(Channel realChannel, final ByteBuffer chunk) throws IOException {
		//copy ByteBuffer here....
        ChannelsRunnable r = new ChannelsRunnable() {
			public void run() {
				try {
					handler.incomingData(channel, chunk);
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
	
	public void farEndClosed(Channel realChannel) {
        ChannelsRunnable r = new ChannelsRunnable() {
			public void run() {
				try {
					handler.farEndClosed(channel);
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

	public void failure(Channel realChannel, final ByteBuffer data, final Exception ee) {
        ChannelsRunnable r = new ChannelsRunnable() {
			public void run() {
				try {
					handler.failure(channel, data, ee);
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

}
