package org.playorm.nio.impl.cm.threaded;

import java.io.IOException;
import java.util.concurrent.Executor;
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
	private Executor svc;

	public ThdProxyWriteHandler(Channel c, OperationCallback h, Executor s) {
		channel = c;
		handler = h;
		svc = s;
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
		svc.execute(r);		
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
		svc.execute(r);
	}

}
