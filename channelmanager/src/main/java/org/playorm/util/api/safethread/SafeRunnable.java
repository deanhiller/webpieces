package org.playorm.util.api.safethread;

import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;



/**
 */
public abstract class SafeRunnable implements Runnable
{
    private static final Logger log = Logger.getLogger(SafeRunnable.class.getName());
    private ExceptionListener listener = NullExceptionListener.singleton();
    private Executor executor;
    private String id;
    
    public SafeRunnable(Executor exec, String id) {
         this(exec, null, id);
    }
    
    public SafeRunnable(Executor exec, ExceptionListener listener, String id) {
        if(id == null)
            throw new IllegalArgumentException("id cannot be null");
        
        if (listener != null)
        	this.listener = listener;
        
        this.id = id;
        this.executor = exec;
    }
    
    public void setExceptionListener(ExceptionListener handler)
    {
    	if(handler!=null)
    		this.listener = handler;
    	else
    		listener= NullExceptionListener.singleton();
    }

    public void run()
    {
        try {
            runImpl();
        } catch(Throwable e) {
            log.log(Level.WARNING, id+"Exception in media", e);
            FireFailureOnClientThread r = new FireFailureOnClientThread(e, id);
            //put on event thread so client can't tie up our threads!!!
            executor.execute(r);
        }
    }

    //NOTE: Cannot extend SafeRunnable as that could cause stackoverflow
    //under the right conditions...ie. if the client keeps throwing an exception
    //which is very likely if it threw an exception the first time you fired a failure
    //to the client.
    private class FireFailureOnClientThread implements Runnable {

        private Throwable e;
        private String id;

        public FireFailureOnClientThread(Throwable e, String id) {
            this.e = e;
            this.id = id;
        }

        public void run() {
            try {
                listener.fireFailure(e, id);
            } catch(Throwable e) {
                log.log(Level.WARNING, id+"Client Exception", e);
            }
        }
    }
    

    protected abstract void runImpl() throws Throwable;

}
