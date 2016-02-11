package org.playorm.nio.impl.cm.threaded;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.deprecated.ChannelServiceFactory;
import org.playorm.nio.api.handlers.DataChunk;
import org.playorm.nio.api.handlers.DataListener;
import org.playorm.nio.api.libs.BufferFactory;
import org.playorm.nio.api.libs.BufferHelper;
import org.playorm.nio.api.libs.ChannelsRunnable;


class ThdProxyDataHandler implements DataListener {

	private static final Logger log = Logger.getLogger(ThdProxyDataHandler.class.getName());
	private static final BufferHelper HELPER = ChannelServiceFactory.bufferHelper(null);
	private ThdTCPChannel channel;
	private DataListener handler;
	private Executor svc;
	private BufferFactory bufFactory;

	public ThdProxyDataHandler(ThdTCPChannel channel, DataListener handler, Executor svc, BufferFactory bufFactory) {
		this.channel = channel;
		this.handler = handler;
		this.svc = svc;
		this.bufFactory = bufFactory;
	}
	
	public void incomingData(Channel realChannel, final DataChunk chunk) throws IOException {
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
		svc.execute(r);	
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
		svc.execute(r);			
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
		svc.execute(r);	
	}

}
