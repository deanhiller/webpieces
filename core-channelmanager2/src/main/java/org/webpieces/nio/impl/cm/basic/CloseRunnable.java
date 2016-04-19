package org.webpieces.nio.impl.cm.basic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.util.futures.Promise;


public class CloseRunnable implements DelayedWritesCloses {

	private static final Logger log = LoggerFactory.getLogger(CloseRunnable.class);
	private BasChannelImpl channel;
	private Promise<Channel, FailureInfo> handler;
    
	public CloseRunnable(BasChannelImpl c, Promise<Channel, FailureInfo> h) {
		channel = c;
		handler = h;
	}

	public boolean runDelayedAction(boolean isSelectorThread) {

        if(log.isTraceEnabled())
            log.trace(channel+"Closing channel.  isOnSelectThread="+isSelectorThread);
        
		try {
			channel.closeImpl();
            
            //must wake up selector or socket will not send the TCP FIN packet!!!!! 
            //The above only happens on the client thread...on selector thread, close works fine.
            channel.wakeupSelector();
            
            handler.setResult(channel);
		} catch(Exception e) {
			log.warn(channel+"Exception occurred", e);
			handler.setFailure(new FailureInfo(channel, e));
		}
		return true;
	}

}
