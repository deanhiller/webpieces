package org.playorm.nio.impl.cm.routing;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.handlers.OperationCallback;
import org.playorm.nio.api.libs.ChannelsRunnable;


public class ThdProxyWriteHandler implements OperationCallback {

	private static final Logger log = Logger.getLogger(ThdProxyWriteHandler.class.getName());
	
	private Channel channel;
	private OperationCallback handler;
	private SpecialRoutingExecutor svc;

	public ThdProxyWriteHandler(Channel c, OperationCallback h, SpecialRoutingExecutor svc2) {
		channel = c;
		handler = h;
		svc = svc2;
	}

	public void finished(Channel realChannel) throws IOException {
        ChannelsRunnable r = new ChannelsRunnable() {
			public void run() {
				try {
					handler.finished(channel);
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

	public void failed(RegisterableChannel c, final Throwable e) {
        ChannelsRunnable r = new ChannelsRunnable() {
			public void run() {
				try {
					handler.failed(channel, e);
				} catch (Exception e) {
					log.log(Level.WARNING, channel+"Exception", e);
				}				
			}
			public RegisterableChannel getChannel() {
				return channel;
			}
		};
		svc.execute((Channel) c, r);
	}

}
