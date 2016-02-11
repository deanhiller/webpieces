package org.playorm.nio.api.libs;

import java.util.List;
import java.util.concurrent.Executor;


public interface StartableRouterExecutor {

	public void start(Object chanMgrId);
	public void stop(Object chanMgrId);
    
    /**
     * Because of a special condition in which deadlock can occur, this layer
     * must know if the thread is in the executor service.  If this is not implemented,
     * deadlock can occur.
     * 
     */
    public boolean containsThread(Thread t);
    
	/**
	 * Return a list of SINGLE THREADED executors!!  Not direct, but each one should be a single thread.  We will ensure that
	 * all packets from each channel go to the correct therad so they never get out of order and then you can do SSL or packetizing
	 * on that thread.  These can then feed into a more general thread pool as well after taht where all channels can be on any
	 * thread.  The issue is you do not want the tail end half of a packet racing past the front end getting an out of order issue
	 */
	public List<Executor> getExecutors();
	
}
