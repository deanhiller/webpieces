package org.playorm.nio.api.libs;

import java.util.concurrent.Executor;


/**
 * Unfortunately, sun's ExecutorServices do not have a restartable lifecycle such
 * that you can stop, start, stop, start the ExecutorService, so when the 
 * ChannelManager stops, we shutdown the ExecutorService and when it starts
 * back up, we create a brand new one.
 * 
 * @author dean.hiller
 *
 */
public interface StartableExecutorService extends Executor {

	public void start(Object chanMgrId);
	public void stop(Object chanMgrId);
    
    /**
     * Because of a special condition in which deadlock can occur, this layer
     * must know if the thread is in the executor service.  If this is not implemented,
     * deadlock can occur.
     * 
     */
    public boolean containsThread(Thread t);

    //public Set<Thread> getThreads();
}
