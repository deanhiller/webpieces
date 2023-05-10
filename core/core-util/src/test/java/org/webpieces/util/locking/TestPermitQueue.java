package org.webpieces.util.locking;

import java.util.List;
import org.webpieces.util.futures.XFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;

import org.webpieces.util.SneakyThrow;
import org.webpieces.util.locking.FuturePermitQueue;
import org.webpieces.util.locking.PermitQueue;
import org.webpieces.util.threading.NamedThreadFactory;

public class TestPermitQueue {

	private Executor executor = Executors.newFixedThreadPool(5, new NamedThreadFactory("deansThr"));
	private PermitQueue queue1 = new PermitQueue(1);
	private FuturePermitQueue queue2 = new FuturePermitQueue("mytest", 1);

	private MockService svc = new MockService();
	
	@Test
	public void testPermits1() throws InterruptedException {
		XFuture<Long> future1 = new XFuture<Long>();
		svc.addToReturn(future1);
		XFuture<Long> future2 = new XFuture<Long>();
		svc.addToReturn(future2);
		
		queue1.runRequest(() -> svc.runFunction(1));
		queue1.runRequest(() -> svc.runFunction(2));
		
		List<Integer> results = svc.getAndClear();
		Assert.assertEquals(1,results.size()); 
		Assert.assertEquals(1, results.get(0).intValue());
		
		future1.complete(3L);
		queue1.releasePermit();
		
		List<Integer> results2 = svc.getAndClear();
		Assert.assertEquals(1,results2.size());
		Assert.assertEquals(2, results2.get(0).intValue());
	}
	
	@Test
	public void testReducePermits() throws InterruptedException {
		PermitQueue queue = new PermitQueue(2);
		
		XFuture<Long> future1 = new XFuture<Long>();
		svc.addToReturn(future1);
		svc.addToReturn(future1);
		svc.addToReturn(future1);
		svc.addToReturn(future1);
		
		queue.runRequest(() -> svc.runFunction(1));
		queue.runRequest(() -> svc.runFunction(2));
		queue.runRequest(() -> svc.runFunction(3));
		queue.runRequest(() -> svc.runFunction(4));


		List<Integer> results = svc.getAndClear();
		Assert.assertEquals(2,results.size()); 
		Assert.assertEquals(1, results.get(0).intValue());
		Assert.assertEquals(2, results.get(1).intValue());
		
		queue.modifyPermitPoolSize(-1);
		
		//release two
		queue.releasePermit();
		queue.releasePermit();
		
		//only one will run
		List<Integer> results2 = svc.getAndClear();
		Assert.assertEquals(1,results2.size());
		Assert.assertEquals(3, results2.get(0).intValue());
		
		queue.releasePermit();
		List<Integer> results3 = svc.getAndClear();
		Assert.assertEquals(1,results3.size());
		Assert.assertEquals(4, results3.get(0).intValue());		
	}
	
	@Test
	public void testAddPermits() throws InterruptedException {
		PermitQueue queue = new PermitQueue(1);
		
		XFuture<Long> future1 = new XFuture<Long>();
		svc.addToReturn(future1);
		svc.addToReturn(future1);
		svc.addToReturn(future1);
		svc.addToReturn(future1);
		
		queue.runRequest(() -> svc.runFunction(1));
		queue.runRequest(() -> svc.runFunction(2));
		queue.runRequest(() -> svc.runFunction(3));
		queue.runRequest(() -> svc.runFunction(4));

		List<Integer> results = svc.getAndClear();
		Assert.assertEquals(1,results.size()); 
		Assert.assertEquals(1, results.get(0).intValue());
		
		queue.modifyPermitPoolSize(2);
		
		List<Integer> results2 = svc.getAndClear();
		Assert.assertEquals(2,results2.size());
		Assert.assertEquals(2, results2.get(0).intValue());
		Assert.assertEquals(3, results2.get(1).intValue());
		
		queue.releasePermit();
		
		List<Integer> results3 = svc.getAndClear();
		Assert.assertEquals(1,results3.size());
		Assert.assertEquals(4, results3.get(0).intValue());		
	}
	
	@Test
	public void testPermits() throws InterruptedException {
		
		executor.execute(new MyRunnable(0));
		executor.execute(new MyRunnable(1));
		executor.execute(new MyRunnable(2));

	}
	
	private XFuture<Long> someFunction(int i) {
		System.out.println("function"+i);
		
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			throw SneakyThrow.sneak(e);
		}
		
		XFuture<Long> future = new XFuture<Long>();
		executor.execute(() -> {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				throw SneakyThrow.sneak(e);
			}
			System.out.println("complete"+i);
			future.complete(100L+i);
		});
		
		return future;
	}
	
	@Test
	public void testFuturePermitQueue() throws InterruptedException {
		XFuture<Long> future1 = new XFuture<Long>();
		svc.addToReturn(future1);
		XFuture<Long> future2 = new XFuture<Long>();
		svc.addToReturn(future2);
		XFuture<Long> future3 = new XFuture<Long>();
		svc.addToReturn(future3);
		
		queue2.runRequest(() -> svc.runFunction(1));
		queue2.runRequest(() -> svc.runFunction(2));
		queue2.runRequest(() -> svc.runFunction(3));

		List<Integer> results = svc.getAndClear();
		Assert.assertEquals(1,results.size()); 
		Assert.assertEquals(1, results.get(0).intValue());
		
		future1.complete(3L);
		
		List<Integer> results2 = svc.getAndClear();
		Assert.assertEquals(1,results2.size());
		Assert.assertEquals(2, results2.get(0).intValue());
	}
	
	private class MyRunnable implements Runnable {

		private int id;

		public MyRunnable(int i) {
			this.id = i;
		}

		@Override
		public void run() {
			queue1.runRequest(() -> someFunction(id)).thenApply( longId -> {
				System.out.println("id="+longId);
				return longId;
			});
		}
	}
}
