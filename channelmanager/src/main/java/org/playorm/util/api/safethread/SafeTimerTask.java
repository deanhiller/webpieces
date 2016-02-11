package org.playorm.util.api.safethread;

import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 */
public abstract class SafeTimerTask extends TimerTask
{
    private ExceptionListener listener = NullExceptionListener.singleton();
    private Executor executor;
    private static final Logger log = Logger.getLogger(SafeTimerTask.class.getName());
    private String id;
    
    /**
     * Creates an instance of SafeTimerTask.
     * @param h
     */
    public SafeTimerTask(Executor exec, String id)
    {
    	this(exec, null, id);
    }
    
    public SafeTimerTask(Executor exec, ExceptionListener listener, String id)
    {
    	if(id == null)
            throw new IllegalArgumentException("id cannot be null");
    	
    	if (listener != null)
    		this.listener = listener;
    	
    	this.executor = exec;
        this.id = id;
    }

    /**
     * @see java.util.TimerTask#run()
     */
    @Override
    public void run()
    {
        try
        {
            runImpl();
        }
        catch(Throwable e) {
            log.log(Level.WARNING, "Exception running runnable possibly from the client's code.  Check the stack trace", e);
            FireFailureOnClientThread r = new FireFailureOnClientThread(e);
            //put on event thread so client can't tie up our threads!!!
            executor.execute(r);
        }
    }
    
    private class FireFailureOnClientThread implements Runnable {

        private Throwable e;

        public FireFailureOnClientThread(Throwable e) {
            this.e = e;
        }

        public void run() {
            try {
            	
                listener.fireFailure(e, id);
            } catch(Throwable e) {
                log.log(Level.WARNING, "Exception from client", e);
            }
        }
    }
    
    public void setExceptionHandler(ExceptionListener listener)
    {
    	if(listener!=null)
        this.listener = listener;
    	else
    		this.listener = NullExceptionListener.singleton();
    }
    protected abstract void runImpl() throws Throwable;

}
