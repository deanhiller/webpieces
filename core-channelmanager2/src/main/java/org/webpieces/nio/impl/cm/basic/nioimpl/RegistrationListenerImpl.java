package org.webpieces.nio.impl.cm.basic.nioimpl;

import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.webpieces.nio.api.testutil.nioapi.ChannelRegistrationListener;
import org.webpieces.nio.impl.cm.basic.SelectorManager2;


class RegistrationListenerImpl implements ChannelRegistrationListener {

	private static final Logger log = Logger.getLogger(RegistrationListenerImpl.class.getName());
	private ClosedChannelException exc = null;
	//public IOException ioExc = null;
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
                log.log(Level.WARNING, "Exception occurred.  Will be rethrown on client thread.  Look for that exc also", e);
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
				if(log.isLoggable(Level.FINE))
					log.fine(id+"call wakeup on selector to register for="+runnable);
				s.wakeUpSelector();
				//selector.wakeup();
				if(waitForWakeup)
					this.wait();
			}
		}
		if(exc != null) {
            exc.fillInStackTrace();
			throw exc;
        } else if(runtime != null) {
            runtime.fillInStackTrace();
			throw runtime;		
        }
	}
}
