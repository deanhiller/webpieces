package org.webpieces.nio.impl.cm.basic;

import java.util.concurrent.CompletableFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;


public class CloseRunnable {

	private static final Logger log = LoggerFactory.getLogger(CloseRunnable.class);
	private BasChannelImpl channel;
	private CompletableFuture<Channel> handler;
    
	public CloseRunnable(BasChannelImpl c, CompletableFuture<Channel> future) {
		channel = c;
		handler = future;
	}

	public boolean runDelayedAction() {

        log.trace(()->channel+"Closing channel.");
        
		try {
			channel.closeImpl();
            
            //must wake up selector or socket will not send the TCP FIN packet!!!!! 
            //The above only happens on the client thread...on selector thread, close works fine.
            channel.wakeupSelector();
            
            handler.complete(channel);
		} catch(Exception e) {
			log.error(channel+"Exception occurred", e);
			handler.completeExceptionally(e);
		}
		return true;
	}

}
