package org.webpieces.util.threading;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DirectExecutorService implements ExecutorService {

	@Override
	public void execute(Runnable command) {
		command.run();
	}

	@Override
	public void shutdown() {
	}

	@Override
	public List<Runnable> shutdownNow() {
		return null;
	}

	@Override
	public boolean isShutdown() {
		return false;
	}

	@Override
	public boolean isTerminated() {
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		return false;
	}

	@Override
	public <T> Future<T> submit(Callable<T> task) {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public Future<?> submit(Runnable task) {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		throw new UnsupportedOperationException("not supported yet");
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		throw new UnsupportedOperationException("not supported yet");
	}

}
