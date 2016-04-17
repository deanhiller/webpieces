package org.webpieces.nio.impl.cm.basic;

import java.net.PortUnreachableException;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.exceptions.FailureInfo;
import org.webpieces.util.futures.Promise;


public class WriteRunnable implements DelayedWritesCloses {

	private static final Logger log = Logger.getLogger(WriteRunnable.class.getName());
	private ByteBuffer buffer;
	private Promise<Channel, FailureInfo> handler;
	private BasChannelImpl channel;

	public WriteRunnable(BasChannelImpl c, ByteBuffer b, Promise<Channel, FailureInfo> h) {
		channel = c;
		buffer = b;
		handler = h;
	}

	public boolean runDelayedAction(boolean isSelectorThread) {
		try {

			channel.writeImpl(buffer);

            //log.info("count="+count+++"  remain="+buffer.remaining()+" wasRemain="+remain);
//			log.info(channel+"CCwriter thread id="+id);
        } catch(PortUnreachableException e) {
            //if a client sends a stream of udp, we fire a failure for each one, but only log it
            //at the finest level as these are not really devastating sometimes.  They are really just
            //telling someone that you are sending to a bad port or bad host or unreachable host
            log.log(Level.FINEST,  channel+"Client sent data to a host or port that is not listening " +
                    "to udp, or udp can't get through to that machine", e);
            handler.setFailure(new FailureInfo(channel, e));
		} catch(Exception e) {
			log.log(Level.WARNING, channel+"Fire failure to client", e);
			handler.setFailure(new FailureInfo(channel, e));
			//we failed so return that the write was tried...no more data is going out
            //at least I don't think so...is it different when getting an icmp(PortUnreachableException)?
            return true; 
		}
		if(buffer.hasRemaining())
			return false;
              
		if(log.isLoggable(Level.FINER))
			log.finer(channel+"WriteCloseCallback.finished called on client");
		
		handler.setResult(channel);
		
		return true;
	}

}
