package org.playorm.nio.impl.cm.routing;

import java.util.List;
import java.util.concurrent.Executor;

import org.playorm.nio.api.channels.Channel;
import org.playorm.nio.api.libs.StartableRouterExecutor;


/**
 * This is a proxy executor for a special case to prevent deadlock.  Read
 * the notes in the execute method for more detail
 */
public class SpecialRoutingExecutor
{

    private StartableRouterExecutor executor;
	private int factor;
	private List<Executor> executors;

    /**
     * Creates an instance of ProxyExecutorService.
     * @param executor
     */
    public SpecialRoutingExecutor(StartableRouterExecutor executor)
    {
        this.executor = executor;
    }

    /**
     * @param id
     */
    public void start(Object id)
    {
        executor.start(id);
        executors = executor.getExecutors();
        factor = Integer.MAX_VALUE / executors.size();
    }

    /**
     * @param id
     */
    public void stop(Object id)
    {
        executor.stop(id);
        executors = null;
    }

    /**
     * @see java.util.concurrent.Executor#execute(java.lang.Runnable)
     */
    public void execute(Channel channel, Runnable command)
    {
        //NOTE:This gets a little tricky.  First a background on writes work is needed
        //
        //Because a write is done on the client thread(which is maybe a thread from this
        //threadpool), the basic channelmanager also fires events on that thread resulting
        //in this execute method being called.
        //
        //Now, knowing that this means all the server threads could block on the real executor
        //when we call execute.  Therefore, if we are on a thread that comes from that server
        //pool, we MUST fire the even on that server pool thread and not call execute.  Calling
        //execute in that case will eventually cause deadlock when the queue fills up.
        
    	//TODO: determine requests and responses and unbound the response queue size and only
    	//bound the request queue size!!!!
    	//THEN MOST IMPORTANTLY, have a rejector type code that sends back errors of overloading 
    	//on those requests (or slow them down).
    	
    	//thinking more on this, maybe channelmanager should NOT have a threadpool.  That is something
    	//that the application layer should have!!!
    	
        Thread t = Thread.currentThread();
        if(executor.containsThread(t)) {
            command.run();
            return;
        }

        int hashCode = channel.hashCode();
        int threadId = hashCode / factor;

        Executor executor = executors.get(threadId);
        executor.execute(command);
    }
    
//    private static final Logger log = Logger.getLogger(SpecialExecutor.class.getName());
//    
//    private void dumpThreads() {
//        log.info("THREAD DUMP");
//        Map<Thread, StackTraceElement[]> allStackTraces = Thread.getAllStackTraces();
//        Set<Thread> threads = allStackTraces.keySet();
//        for(Thread t : threads) {
//            log.info("t name="+t.getName()+" id="+t.getId()+" t="+t+" state="+t.getState());
//            StackTraceElement[] lines = allStackTraces.get(t);
//            for(StackTraceElement line : lines) {
//                System.err.println("\t"+line);
//            }
//        }
//    }
}
