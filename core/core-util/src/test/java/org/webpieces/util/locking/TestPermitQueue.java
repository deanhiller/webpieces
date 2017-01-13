package org.webpieces.util.locking;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Test;
import org.webpieces.util.threading.NamedThreadFactory;

import com.webpieces.util.locking.PermitQueue;

public class TestPermitQueue {

	private Executor executor = Executors.newFixedThreadPool(5, new NamedThreadFactory("deansThr"));
	private PermitQueue<Long> queue = new PermitQueue<>(executor, 1);
	
	@Test
	public void testPermits() throws InterruptedException {
		
		executor.execute(new MyRunnable(0));
		executor.execute(new MyRunnable(1));
		executor.execute(new MyRunnable(2));

	}
	
	private CompletableFuture<Long> someFunction(int i) {
		System.out.println("function"+i);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
		
		CompletableFuture<Long> future = new CompletableFuture<Long>();
		executor.execute(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			System.out.println("complete"+i);
			future.complete(100L+i);
		});
		
		return future;
	}
	
	private class MyRunnable implements Runnable {

		private int id;

		public MyRunnable(int i) {
			this.id = i;
		}

		@Override
		public void run() {
			queue.runRequest(() -> someFunction(id)).thenApply( longId -> {
				System.out.println("id="+longId);
				return longId;
			});
		}
	}
}
