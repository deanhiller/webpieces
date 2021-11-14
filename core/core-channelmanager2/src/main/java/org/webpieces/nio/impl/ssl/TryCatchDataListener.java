package org.webpieces.nio.impl.ssl;

import java.nio.ByteBuffer;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.channels.Channel;
import org.webpieces.nio.api.handlers.DataListener;

/**
 * A class purely for catching exceptions the client forgets to catch
 * 
 * @author dhiller
 *
 */
public class TryCatchDataListener implements DataListener {

	private static final Logger log = LoggerFactory.getLogger(TryCatchDataListener.class);
	private DataListener dataListener;
	
	public TryCatchDataListener(DataListener dataListener) {
		this.dataListener = dataListener;
	}

	@Override
	public XFuture<Void> incomingData(Channel channel, ByteBuffer b) {
		try {
			return dataListener.incomingData(channel, b);
		} catch(Throwable e) {
			log.error("Exception", e);
			XFuture<Void> fut = new XFuture<Void>();
			fut.completeExceptionally(e);
			return fut;
		}
	}

	@Override
	public void farEndClosed(Channel channel) {
		try {
			dataListener.farEndClosed(channel);
		} catch(Throwable e) {
			log.error("Exception", e);
		}
	}

	@Override
	public void failure(Channel channel, ByteBuffer data, Exception e) {
		try {
			dataListener.failure(channel, data, e);
		} catch(Throwable ee) {
			log.error("Exception caught trying to handle the other exception(the other exception IS more important)", ee);
		}
	}

}
