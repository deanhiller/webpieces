package org.playorm.nio.api.mgmt;

import biz.xsoftware.api.platform.mgmt.Documentation;

/**
 * Specific to the ChannelManagerService's ExecutorService implementation
 * 
 * @author dean.hiller
 */
public interface ExecutorServiceMBean {

	@Documentation("true if the threads are running and grabbing tasks off queue.  false otherwise")
	public boolean isRunning();
	
	@Documentation("The prefix name of all the threads in the threadpool")
	public String getName();

	@Documentation("From jdk docs: The maximum allowed number of threads.  Can be changed while running")
	public int getMaximumPoolSize();
	public void setMaximumPoolSize(int numThreads);

	@Documentation("From jdk docs: The core number of threads.  Can be changed while running")
	public int getCorePoolSize();
	public void setCorePoolSize(int numThreads);
	
	@Documentation("The count of the current threads in the pool.  -1 if not running")
	public int getPoolSize();
	
	@Documentation("The largest number of threads that have ever simultaneously"+
			" been in the pool since starting, or since changing queue size.  -1 if not running")
	public int getLargestPoolSize();

	@Documentation("The approximate number of threads that are actively executing tasks.")
	public int getActiveCount();
	
	@Documentation("Whether are not the threads in the pool are Daemon threads")
	public boolean isDaemonThreads();
	
	@Documentation("The approximate total number of tasks that have completed execution" +
			" since starting, or since changing queue size.  -1 if not running")
	public long getCompletedTaskCount();

	@Documentation("The thread keep-alive time, which is the amount of time which" +
			" threads in excess of the core pool size may remain idle before" +
			" being terminated.  Can be changed while running.")
	public long getKeepAliveTime();
	public void setKeepAliveTime(long time);

	@Documentation("The approximate total number of tasks that have been" +
			" scheduled for execution since starting or since changing queue size")
	public long getTaskCount();
	
	
	
	@Documentation("The number of Runnables the Queue can hold while the threads" +
			"in the threadpool process other tasks.  Can be changed while running")
	public int getQueueSize();
	public void setQueueSize(int max);
	
	@Documentation("Remaining space in the queue that the threads in the thread pool" +
			"read from.  When this reaches 0, the producer thread will stop feeding the" +
			"threadpool more tasks to run.  If it reaches 0 often, the QueueSize property" +
			"should be changed.  Returns -1 if ExecutorService is not running")
	public int getRemainingCapacity();	
	
	@Documentation("The current count of all the Runnables in the queue that have not" +
			"been taken off the queue by threads in the threadpool")
	public int getCurrentSize();
	
}
