package org.playorm.nio.impl.cm.basic.nioimpl;

import java.nio.channels.ClosedChannelException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.testutil.nioapi.ChannelRegistrationListener;
import org.playorm.nio.api.testutil.nioapi.SelectorRunnable;
import org.playorm.nio.impl.cm.basic.SelectorManager2;


class RegistrationListenerImpl implements ChannelRegistrationListener {

	private static final Logger log = Logger.getLogger(RegistrationListenerImpl.class.getName());
	private ClosedChannelException exc = null;
	//public IOException ioExc = null;
	private RuntimeException runtime = null;
	private boolean processed = false;
	private SelectorRunnable r;
	private SelectorManager2 s;
	private Object id;

	public RegistrationListenerImpl(Object id, SelectorRunnable r, SelectorManager2 s) {
		this.id = id;
		this.r = r;
		this.s = s;
	}
	
	public void processRegistrations() {
			
		if(!processed) {
			try {
				r.run();
			} catch(ClosedChannelException e) {
                log.log(Level.WARNING, "Exception occurred.  Will be rethrown on client thread.  Look for that exc also", e);
				exc = e;
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
					log.fine(id+"call wakeup on selector to register for="+r);
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
