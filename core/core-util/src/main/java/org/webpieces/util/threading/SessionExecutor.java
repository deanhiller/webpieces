package org.webpieces.util.threading;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Shares a threadpool and all the threads in a unique way that each key gets a virtual thread which
 * really means it uses all threads in the thread pool but guarantees order of incoming Runnables on a
 * per key basis so anything with the same key that comes in 1, 2, 3 is run in the thread pool 1, 2, 3
 * but not necessarily on the same thread.  ie. the thread is virtually intact as 1, 2, 3 runs in order
 * 
 * @author dhiller
 *
 */
public interface SessionExecutor {

	public <T> CompletableFuture<T> execute(Object key, Callable<CompletableFuture<T>> r);

	public void execute(Object key, Runnable r);

	
}
