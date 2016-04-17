package org.webpieces.nio.impl.cm.basic;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.util.futures.Promise;


public class CloseRunnable implements DelayedWritesCloses {

	private static final Logger log = Logger.getLogger(CloseRunnable.class.getName());
	private BasChannelImpl channel;
	private Promise<Channel, FailureInfo> handler;
    
	public CloseRunnable(BasChannelImpl c, Promise<Channel, FailureInfo> h) {
		channel = c;
		handler = h;
	}

	public boolean runDelayedAction(boolean isSelectorThread) {

        if(log.isLoggable(Level.FINER))
            log.finer(channel+"Closing channel.  isOnSelectThread="+isSelectorThread);
        
		try {
			channel.closeImpl();
            
            //must wake up selector or socket will not send the TCP FIN packet!!!!! 
            //The above only happens on the client thread...on selector thread, close works fine.
            channel.wakeupSelector();
            
            handler.setResult(channel);
		} catch(Exception e) {
			log.log(Level.WARNING, channel+"Exception occurred", e);
			handler.setFailure(new FailureInfo(channel, e));
		}
		return true;
	}

}
