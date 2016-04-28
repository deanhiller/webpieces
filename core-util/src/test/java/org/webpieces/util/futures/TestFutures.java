package org.webpieces.util.futures;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.junit.Assert;
import org.junit.Test;

public class TestFutures {

	@Test
	public void testSetResultBeforeFunction() {
		PromiseImpl<Integer> op = new PromiseImpl<>(null);
		op.setResult(5);
		
		final List<Integer> values = new ArrayList<>();
		Future<Integer> future = op;
		future.setResultFunction(p -> values.add(p));
		
		Assert.assertEquals(5, values.get(0).intValue());
		
		op.setResult(85);
		
		Assert.assertEquals(1, values.size());
	}

	@Test
	public void testSetResultAfterFunction() {
		PromiseImpl<Integer> op = new PromiseImpl<>(null);
		
		final List<Integer> values = new ArrayList<>();
		Future<Integer> future = op;
		future.setResultFunction(p -> values.add(p));
		
		op.setResult(4);
		Assert.assertEquals(4, values.get(0).intValue());
		
		op.setResult(3);
		Assert.assertEquals(1, values.size());
	}
	
	public void testBlowTheStack() throws InterruptedException {
		recurse(0);
		
		System.out.println("done");
		Thread.sleep(5000);
	}
	

	private void recurse(int counter) {
		CompletableFuture<Integer> future = writeData(counter);
		future.thenAccept(p -> recurse(p+1)).join();		
	}
	
	private CompletableFuture<Integer> writeData(int counter) {
		System.out.println("counter="+counter);
		if(counter == 1455) {
			System.out.println("mark");
		}
		
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
		future.complete(counter);
		return future;
	}
	
	@Test
	public void testCompletableFuture() throws InterruptedException {
		System.out.println("thread="+Thread.currentThread());
		CompletableFuture<Integer> myFuture = new CompletableFuture<>();
		myFuture.thenAccept(intResult -> log(intResult));
		CompletableFuture<String> result = myFuture.thenApply(p -> translate(p));
		
		result.handle((r, e) -> handle(r, e));
		
		result.cancel(true);
		
		Thread.sleep(5000);
		
		myFuture.complete(8);
		
		Thread.sleep(10000);
	}

	private String handle(String r, Throwable e) {
		if(r != null) {
			System.out.println("result");
		} else {
			System.out.println("FINISHED fail.  thread="+Thread.currentThread());
			e.printStackTrace();
		}
		
		return "done";
	}
	
	private void log(Integer intResult) {
		System.out.println("int result="+intResult+" thread="+Thread.currentThread());
	}

	private String translate(Integer p) {
		System.out.println("(translate)thread="+Thread.currentThread());
		return p+"str";
	}
	
}
