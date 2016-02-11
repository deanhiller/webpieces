package org.playorm.nio.impl.libs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.playorm.nio.api.libs.StartableRouterExecutor;


/**
 */
public class RoutingExecutor implements StartableRouterExecutor
{
    private int queueSize = 300;
    private long keepAliveTime = 5000;
    private boolean isDaemon = true;
    private String name;
    private List<ThreadPoolExecutor> singleThreadedExecutors = new ArrayList<ThreadPoolExecutor>();
    private MyThreadFactory threadFactory;
	private int numThreads;

    public RoutingExecutor(String name, int numThreads) {
    	this.name = name;
        this.numThreads = numThreads;
    }

    public void start(Object chanMgrId) {
        threadFactory = new MyThreadFactory(name, isDaemon);
        
        for(int i = 0; i < numThreads; i++) {
	        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueSize);
	        ThreadPoolExecutor executor = new ThreadPoolExecutor(1, 1,
	                keepAliveTime , TimeUnit.MILLISECONDS, queue, threadFactory);
	        singleThreadedExecutors.add(executor);
        }
    }
    
    public void stop(Object chanMgrId) {
    	for(ThreadPoolExecutor exec : singleThreadedExecutors) {
    		exec.shutdownNow();
    	}
    }

    /**
     * @see org.playorm.nio.api.libs.StartableExecutorService#containsThread(java.lang.Thread)
     */
    public boolean containsThread(Thread t)
    {
        return threadFactory.containsThread(t);
    }

	@Override
	public List<Executor> getExecutors() {
		List<Executor> executors = new ArrayList<Executor>();
		for(ThreadPoolExecutor exec: singleThreadedExecutors) {
			executors.add(exec);
		}
		return executors;
	}
}
