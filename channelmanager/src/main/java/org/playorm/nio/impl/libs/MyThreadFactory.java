/**
 * 
 */
package org.playorm.nio.impl.libs;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.playorm.nio.api.libs.ChannelSession;
import org.playorm.nio.api.libs.MutableSessionThread;
import org.playorm.nio.api.libs.SessionContext;


class MyThreadFactory implements ThreadFactory {

    private static final Logger log = Logger.getLogger(MyThreadFactory.class.getName());
    private Set<Thread> threads = new HashSet<Thread>();
	private int counter = 0;
	private Object id;
	private boolean isDaemon;
	public MyThreadFactory(Object id, boolean isDaemon) {
		this.id = id;
		this.isDaemon = isDaemon;
		SessionsImpl.init();
	}
	public Thread newThread(Runnable r) {
        Notifier n = new Notifier(r);
		Thread t = new SessionThreadImpl(n);
        n.setThread(t);
		t.setName(id+""+counter++);
		t.setDaemon(isDaemon); //set to child thread so this thread doesn't keep jvm running
        
        if(log.isLoggable(Level.FINE))
            log.fine("returning t="+t);
        threads.add(t);
		return t;
	}
	
    private class Notifier implements Runnable {
        private Runnable runnable;
        private Thread thread;

        public Notifier(Runnable r) {
            this.runnable = r;
        }

        /**
         * @param t
         */
        public void setThread(Thread t)
        {
            thread = t;
        }

        public void run() {
            try {
                runnable.run();
            } finally {
                threads.remove(thread);
            }
        }
    }
    
	private class SessionThreadImpl extends Thread implements MutableSessionThread {
		private SessionContext state;
		private ChannelSession session;
		
		public SessionThreadImpl(Runnable r) {
			super(r);
		}

		public void setSessionState(SessionContext s) {
			state = s;
		}

		public SessionContext getSessionState() {
			return state;
		}

		public ChannelSession getSession() {
			return session;
		}

		public void setSession(ChannelSession s) {
			session = s;
		}
	}

    public boolean containsThread(Thread t) {
        return threads.contains(t);
    }

//    public Set<Thread> getThreads()
//    {
//        return new HashSet<Thread>(threads);
//    }
}