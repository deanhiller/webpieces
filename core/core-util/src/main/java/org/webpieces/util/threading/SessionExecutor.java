package org.webpieces.util.threading;

public interface SessionExecutor {

	//public <T>CompletableFuture<T> execute(Object key, Callable<T> r);

	public void execute(Object key, Runnable r);

	
}
