package org.playorm.nio.impl.libs;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.playorm.nio.api.libs.StartableExecutorService;


/**
 */
public class AdvancedExecutorService implements StartableExecutorService
{
    private int numThreads;

    private int queueSize = 300;
    private long keepAliveTime = 5000;
    private boolean isDaemon = true;
    private String name;
    private ThreadPoolExecutor executor;

    private MyThreadFactory threadFactory;

    public AdvancedExecutorService(int numThreads) {
        this.numThreads = numThreads;
    }

    public void start(Object chanMgrId) {
        threadFactory = new MyThreadFactory(name, isDaemon);
        ArrayBlockingQueue<Runnable> queue = new ArrayBlockingQueue<Runnable>(queueSize);
        LookAheadQueue proxy = new LookAheadQueue(queue);
        executor = new ThreadPoolExecutor(numThreads, numThreads,
                keepAliveTime , TimeUnit.MILLISECONDS, proxy, threadFactory);
    }   
    
    public void stop(Object chanMgrId) {
        throw new UnsupportedOperationException("not supported yet");
    }

    public void execute(Runnable command)
    {
        executor.execute(command);
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
