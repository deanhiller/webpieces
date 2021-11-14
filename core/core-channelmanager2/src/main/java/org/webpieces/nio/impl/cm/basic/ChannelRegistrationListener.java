package org.webpieces.nio.impl.cm.basic;

import java.util.EventListener;
import org.webpieces.util.futures.XFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public abstract class ChannelRegistrationListener implements EventListener {

	private static final Logger log = LoggerFactory.getLogger(ChannelRegistrationListener.class);
	private XFuture<Void> future;
	private int validOps;
	
	public ChannelRegistrationListener(XFuture<Void> future, int validOps) {
		this.future = future;
		this.validOps = validOps;
	}
	
	public final void processRegistrations() {
		try {
			run();
			future.complete(null);
		} catch(Throwable e) {
			log.error("Exception completing", e);
			future.completeExceptionally(e);
		}
	}

	protected abstract void run();

	@Override
	public String toString() {
		return OpType.opType(validOps);
	}
	
}
