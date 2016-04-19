package org.webpieces.nio.impl.cm.basic.nioimpl;

import java.nio.channels.ClosedChannelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.nio.api.testutil.nioapi.ChannelRegistrationListener;
import org.webpieces.nio.impl.cm.basic.SelectorManager2;


class RegistrationListenerImpl implements ChannelRegistrationListener {

	private static final Logger log = LoggerFactory.getLogger(RegistrationListenerImpl.class);
	private RuntimeException runtime = null;
	private boolean processed = false;
	private Runnable runnable;
	private SelectorManager2 s;
	private Object id;

	public RegistrationListenerImpl(Object id, Runnable r, SelectorManager2 s) {
		this.id = id;
		this.runnable = r;
		this.s = s;
	}
	
	public void processRegistrations() {
			
		if(!processed) {
			try {
				runnable.run();
			} catch(RuntimeException e) {
                log.warn("Exception occurred.  Will be rethrown on client thread.  Look for that exc also", e);
				runtime = e;
			}
			synchronized(this) {
				processed = true;
				this.notify();
			}
		}
	}

	public void waitForFinish(boolean waitForWakeup) throws InterruptedException, ClosedChannelException {

		synchronized(this) {

			if(!processed) {
				if(log.isTraceEnabled())
					log.trace(id+"call wakeup on selector to register for="+runnable);
				s.wakeUpSelector();
				//selector.wakeup();
				if(waitForWakeup)
					this.wait();
			}
		}

        if(runtime != null) {
            runtime.fillInStackTrace();
			throw runtime;		
        }
	}
}
