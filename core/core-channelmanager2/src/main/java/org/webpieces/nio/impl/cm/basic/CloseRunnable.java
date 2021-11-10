package org.webpieces.nio.impl.cm.basic;

import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CloseRunnable {

	private static final Logger log = LoggerFactory.getLogger(CloseRunnable.class);
	private BasChannelImpl channel;
	private XFuture<Void> handler;
    
	public CloseRunnable(BasChannelImpl c, XFuture<Void> future) {
		channel = c;
		handler = future;
	}

	public boolean runDelayedAction() {

        if(log.isTraceEnabled())
			log.trace(channel+"Closing channel.");
        
		try {
			channel.closeImpl();
            
            //must wake up selector or socket will not send the TCP FIN packet!!!!! 
            //The above only happens on the client thread...on selector thread, close works fine.
            channel.wakeupSelector();
            
            handler.complete(null);
		} catch(Exception e) {
			log.error(channel+"Exception occurred", e);
			handler.completeExceptionally(e);
		}
		return true;
	}

}
