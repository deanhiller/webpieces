package org.webpieces.nio.impl.cm.basic.nioimpl;

import java.util.EventListener;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.impl.cm.basic.Helper;


public abstract class ChannelRegistrationListener implements EventListener {

	private static final Logger log = LoggerFactory.getLogger(ChannelRegistrationListener.class);
	private CompletableFuture<Void> future;
	private int validOps;
	
	public ChannelRegistrationListener(CompletableFuture<Void> future, int validOps) {
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
		return Helper.opType(validOps);
	}
	
}
