/**
 * 
 */
package org.playorm.nio.impl.cm.basic;

import java.nio.channels.SelectionKey;
import java.util.logging.Logger;

import org.playorm.nio.api.channels.RegisterableChannel;
import org.playorm.nio.api.handlers.ConnectionListener;
import org.playorm.nio.api.handlers.DataListener;


public class WrapperAndListener {

	private static final Logger log = Logger.getLogger(WrapperAndListener.class.getName());
	private String channelName;
	private RegisterableChannel channel;
	private DataListener dataHandler;
	private ConnectionListener connectCallback;
	private ConnectionListener acceptCallback;
	
	public WrapperAndListener(RegisterableChannelImpl r) {
		if(r == null)
			throw new IllegalArgumentException("r cannot be null, bug");
		channel = r;
		channelName = ""+channel;
	}

	public String toString() {
		return channelName;
	}

	public void addListener(Object id, Object l, int validOps) {
		//cannot do instanceof here as clients may use one object as two instances and it
		//could be set wrong or twice.....ie. we can't tell.  instead use validOps
		switch(validOps) {
		case SelectionKey.OP_ACCEPT:
			if(acceptCallback != null)
				throw new RuntimeException(channel+"ConnectionListener is already set, cannot be set again");
			acceptCallback = (ConnectionListener)l;
			break;
		case SelectionKey.OP_CONNECT:
			if(connectCallback != null)
				throw new RuntimeException(channel+"ConnectionListener is already set, cannot be set again");
			connectCallback = (ConnectionListener)l;
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
	
	public ConnectionListener getAcceptCallback() {
		return acceptCallback;
	}

	public RegisterableChannel getChannel() {
		return channel;
	}

	public ConnectionListener getConnectCallback() {
		return connectCallback;
	}

	public DataListener getDataHandler() {
		return dataHandler;
	}

}