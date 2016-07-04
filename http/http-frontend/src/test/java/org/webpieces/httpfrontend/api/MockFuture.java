package org.webpieces.httpfrontend.api;

import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class MockFuture<V> implements ScheduledFuture<V> {

	private boolean isCancelled;

	@Override
	public long getDelay(TimeUnit unit) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(Delayed o) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		isCancelled = true;
		return false;
	}

	@Override
	public boolean isCancelled() {
		return isCancelled;
	}

	@Override
	public boolean isDone() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public V get() throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public V get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		// TODO Auto-generated method stub
		return null;
	}

}
