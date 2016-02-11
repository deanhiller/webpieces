package org.playorm.nio.impl.libs;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.playorm.nio.api.libs.StartableExecutorService;
import org.playorm.nio.api.mgmt.ExecutorServiceMBean;


public class ExecutorServiceProxy implements StartableExecutorService, ExecutorServiceMBean {
	
//	private static final Logger log = Logger.getLogger(ExecutorServiceProxy.class.getName());
	private ThreadPoolExecutor realSvc;
    private MyThreadFactory threadFactory;
    
	private int maximumPoolSize;
	private int corePoolSize;	
	private boolean isDaemon = true;
	private String name;
	private int queueSize = 300;
	private long keepAliveTime = 5000;
	private boolean isRunning = false;
	
	public ExecutorServiceProxy(int numThreads) {
		this.maximumPoolSize = numThreads;
		this.corePoolSize = numThreads;
	}
	
	public synchronized void start(Object chanMgrId) {
		name = ""+chanMgrId;
        threadFactory = new MyThreadFactory(name, isDaemon);
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueSize);
        BlockingQueue<Runnable> blockingQueue = new MyBlockingQueue<Runnable>(queue);
        ThreadPoolExecutor svc = new ThreadPoolExecutor(maximumPoolSize, maximumPoolSize,
        		keepAliveTime , TimeUnit.MILLISECONDS, blockingQueue, threadFactory);
		realSvc = svc;
		isRunning = true;
	}

	public synchronized void stop(Object chanMgrId) {
		if(realSvc != null)
			realSvc.shutdownNow();
		realSvc = null;
		isRunning = false;
	}

	public synchronized void execute(Runnable command) {
        if(!isRunning)
            throw new IllegalStateException("This service was not started");

        realSvc.execute(command);
	}
	//manageability stuff is below....
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public String getName() {
		return name;
	}
	public boolean isDaemonThreads() {
		return isDaemon;
	}
	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}
	public synchronized void setMaximumPoolSize(int numThreads) {
		this.maximumPoolSize = numThreads;
		if(realSvc != null)
			realSvc.setMaximumPoolSize(maximumPoolSize);
	}
	public int getCorePoolSize() {
		return corePoolSize;
	}
	public synchronized void setCorePoolSize(int numThreads) {
		corePoolSize = numThreads;
		if(realSvc != null)
			realSvc.setCorePoolSize(corePoolSize);
	}
	public synchronized int getPoolSize() {
		if(realSvc != null)
			return realSvc.getPoolSize();
		return -1;
	}
	public synchronized int getLargestPoolSize() {
		if(realSvc != null)
			return realSvc.getLargestPoolSize();
		return -1;
	}
	public synchronized int getActiveCount() {
		if(realSvc != null)
			return realSvc.getActiveCount();
		return 0;
	}	
	public synchronized long getTaskCount() {
		if(realSvc != null)
			return realSvc.getTaskCount();
		return 0;
	}
	public long getCompletedTaskCount() {
		if(realSvc != null) {
			return realSvc.getCompletedTaskCount();
		}
		return -1;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}
	public synchronized void setKeepAliveTime(long time) {
		keepAliveTime = time;
		if(realSvc != null)
			realSvc.setKeepAliveTime(time, TimeUnit.MILLISECONDS);
	}
		
	public int getQueueSize() {
		return queueSize;
	}
	public void setQueueSize(int numElem) {
		this.queueSize = numElem;
		if(realSvc != null) {
			ExecutorService previous = realSvc;
            MyThreadFactory threadFactory = new MyThreadFactory(name, isDaemon);
            ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueSize);
            ThreadPoolExecutor svc = new ThreadPoolExecutor(maximumPoolSize, maximumPoolSize,
            		keepAliveTime , TimeUnit.MILLISECONDS, queue, threadFactory);
			
			//switching to another executor likes this allows us not to have
			//to synchronize on putting things in the queue...(ie. the execute method)
			realSvc = svc;
			
			//now that the switch has been made, don't allow the one that is going away to 
			//accept any more tasks, BUT allow those tasks to all complete
			previous.shutdown();
		}	
	}
	
	public synchronized int getRemainingCapacity() {
		if(realSvc != null) {
            BlockingQueue<Runnable> queue = (BlockingQueue<Runnable>)realSvc.getQueue();
			return queue.remainingCapacity();
		}
		return -1;
	}
	
	public synchronized int getCurrentSize() {
		if(realSvc != null) {
            BlockingQueue<Runnable> queue = (BlockingQueue<Runnable>)realSvc.getQueue();
			return queue.size();
		}
		return -1;
	}
	

	/**
     * @see org.playorm.nio.api.libs.StartableExecutorService#containsThread(java.lang.Thread)
     */
    public boolean containsThread(Thread t)
    {
        return threadFactory.containsThread(t);
    }	
    
//    public Set<Thread> getThreads() {
//        return threadFactory.getThreads();
//    }
}
