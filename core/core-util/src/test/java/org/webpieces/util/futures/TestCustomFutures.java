package org.webpieces.util.futures;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.junit.Assert;
import org.junit.Test;


public class TestCustomFutures {

	private Executor exec = Executors.newFixedThreadPool(3);
	
	@Test
	public void testFutureContext() throws InterruptedException, ExecutionException {
		
		List<Integer> list = new ArrayList<Integer>();
		
		FutureLocal.put("test", 100);
	
		Set<Integer> nums = new HashSet<>();
		Function<Object, Boolean> cancelFunc = (s) -> {
			System.out.println("cancel="+s);
			nums.add(56);
			return true;
		};
		
		XFuture<Integer> f0 = XFuture.completedFuture(10, cancelFunc);
		XFuture<Integer> f = f0.thenCompose(s -> myRemoteCall(4, list));				
		XFuture<Integer> f2 = f.thenCompose(s -> myRemoteCall(3, list));
		XFuture<Integer> f3 = f2.thenApply(s -> myLocalCall(1, list));
		XFuture<Integer> f4 = f3.thenCompose(s -> myRemoteCall(2, list));
		XFuture<Integer> f5 = f4.thenApply(s -> myLocalCall(1, list));
		XFuture<Integer> f6 = f5.thenCompose(s -> myRemoteCall(0, list));
		
		f6.get();
		
		Assert.assertEquals(6, list.size()); //make sure it was stored 3 times not 2 and that context is working
		
		Assert.assertEquals(0,  nums.size()); //cancel has not been called so this is 0 right now
		//Test a POST cance as well
		f6.cancelChain("Reason for cancel");
		
		Assert.assertEquals(1, nums.size()); //cancel function above fills in the nums
	}
	
	private Integer myLocalCall(int i, List<Integer> list) {
		System.out.println("result="+i+" map="+FutureLocal.get("test")+" thread="+Thread.currentThread().getName());

		Integer value = (Integer) FutureLocal.get("test");
		if(value != null)
			list.add(value);
		
		return i;
	}
	
	private XFuture<Integer> myRemoteCall(int i, List<Integer> list) {
		System.out.println("result="+i+" map="+FutureLocal.get("test")+" thread="+Thread.currentThread().getName());

		Integer value = (Integer) FutureLocal.get("test");
		if(value != null)
			list.add(value);
		
		XFuture<Integer> f = new XFuture<Integer>();
		
		exec.execute(new Runnable() {
			
			@Override
			public void run() {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					f.completeExceptionally(e);
				}
				
				f.complete(i);
			}
		});
		
		return f;
	}
}
