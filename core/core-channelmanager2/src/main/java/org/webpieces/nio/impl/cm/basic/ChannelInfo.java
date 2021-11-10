/**
 * 
 */
package org.webpieces.nio.impl.cm.basic;

import java.nio.channels.SelectionKey;
import org.webpieces.util.futures.XFuture;

import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.channels.RegisterableChannel;
import org.webpieces.nio.api.handlers.DataListener;


public class ChannelInfo {

	//private static final Logger log = LoggerFactory.getLogger(WrapperAndListener.class);
	private String channelName;
	private RegisterableChannel channel;
	private DataListener dataHandler;
	private XFuture<Channel> connectCallback;
	
	public ChannelInfo(RegisterableChannelImpl r) {
		if(r == null)
			throw new IllegalArgumentException("r cannot be null, bug");
		channel = r;
		channelName = ""+channel;
	}

	public String toString() {
		return channelName;
	}

	@SuppressWarnings("unchecked")
	public void addListener(Object l, int validOps) {
		//cannot do instanceof here as clients may use one object as two instances and it
		//could be set wrong or twice.....ie. we can't tell.  instead use validOps
		switch(validOps) {
		case SelectionKey.OP_ACCEPT:
			break;
		case SelectionKey.OP_CONNECT:
			if(connectCallback != null)
				throw new RuntimeException(channel+"ConnectionListener is already set, cannot be set again");
			connectCallback = (XFuture<Channel>)l;
			break;
		case SelectionKey.OP_READ:
			if(dataHandler != null) {
				//A VERY BAD hack for now.  When SSL layer fires connected, the client calls registerForRead which calls this addListener
				//method BUT then when we return down the stack, the SSL layer calls DataChunk.setProcessed() which is required to notify
				//bottom basic layer to start reading again EXCEPT that results in two calls to this method which I don't like.
				return;
//				if(!dataHandler.equals(l)) //we only throw if it is NOT the exact same listener
//					throw new RuntimeException(channel+"DataListener is already set, cannot be set again");
			}
			dataHandler = (DataListener)l;
			break;
		case SelectionKey.OP_WRITE:
		    //okay to ignore and do nothing...not used later
			break;
		default:
			throw new IllegalArgumentException("type="+l.getClass().getName()+" is not allowed");
		}
	}

	public void removeListener(int ops) {
		if((ops & SelectionKey.OP_READ) > 0)
			dataHandler = null;
	}
	
	public RegisterableChannel getChannel() {
		return channel;
	}

	public XFuture<Channel> getConnectCallback() {
		return connectCallback;
	}

	public DataListener getDataHandler() {
		return dataHandler;
	}

}