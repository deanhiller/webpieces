package org.webpieces.util.threading;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

/**
 * Shares a threadpool and all the threads in a unique way that each key gets a virtual thread which
 * really means it uses all threads in the thread pool but guarantees order of incoming Runnables on a
 * per key basis so anything with the same key that comes in 1, 2, 3 is run in the thread pool 1, 2, 3
 * but not necessarily on the same thread.  ie. the thread is virtually intact as 1, 2, 3 runs in order.
 * This also means a bad client(associated with a key) has a very hard time starving other clients such
 * that resource sharing is fair(at least if used with sockets, fair between sockets as a client could then
 * open up many sockets) 
 * 
 * At any rate this allows us to do SSL handshake, http1 parsing, http2 parsing in a threadpool while keeping
 * things in order
 * 
 * @author dhiller
 *
 */
public interface SessionExecutor {

	public <T> CompletableFuture<T> executeCall(Object key, Callable<CompletableFuture<T>> r);

	public void execute(Object key, Runnable r);

	
}
