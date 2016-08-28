package org.webpieces.util.futures;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

public class TestFutures {

	//private static final Logger log = LoggerFactory.getLogger(TestFutures.class);

	@Test
	public void testRegisterTwoListenersToSameFuture() {
		CompletableFuture<Integer> future = new CompletableFuture<Integer>();

		
		future.thenAccept(f -> fire(f));
		future.thenAccept(f -> fire2(f));
		future.thenAccept(f -> fire(f+2));
		future.thenAccept(f -> fire(f+3));

		future.complete(5);
	}
	
	private Object fire2(Integer f) {
		System.out.println("fire2="+f);
		return null;
	}

	private Object fire(Integer f) {
		System.out.println("f="+f);
		return null;
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
	
	/**
	 * Some times you want code to run success or fail and to propagate so here is how
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testAFinallyMethodWithCompletableFutures() throws InterruptedException, ExecutionException {
		CompletableFuture<Integer> myFuture = new CompletableFuture<>();
		CompletableFuture<Integer> newFuture = myFuture.thenApply(intResult -> throwException());
		
		myFuture.complete(5);

		Assert.assertFalse(myFuture.isCompletedExceptionally());
		Assert.assertTrue(newFuture.isCompletedExceptionally());
		
		CompletableFuture<Integer> future2 = newFuture.handle((r, e) -> {
			if(r != null)
				return r;
			else if(e != null)
				throw new RuntimeException(e);
			else
				throw new RuntimeException("weird");
		});
		
		Assert.assertTrue(future2.isCompletedExceptionally());
		
		
		CompletableFuture<Object> future = newFuture.handle((r, e) -> {
			if(r != null)
				return r;
			else if(e != null)
				return e;
			else
				return new RuntimeException("Asdf");			
		});
		
		Assert.assertTrue(future.get() instanceof RuntimeException);
		Assert.assertFalse(future.isCompletedExceptionally());

		
	}
	
	public Integer throwException() {
		throw new RuntimeException("hit here");
	}
	
//	@Test
//	public void testExecutor() throws InterruptedException {
//		CompletableFuture<Integer> future = new CompletableFuture<Integer>();
//
//		future.complete(6);
//		
//		ExecutorService executor = Executors.newFixedThreadPool(5, new NamedThreadFactory("hithere"));
//		future.thenApplyAsync(v -> logIt(v), executor);
//		
//		Thread.sleep(5000);
//	}
//	
//	public int logIt(int v) {
//		log.info("thread="+Thread.currentThread()+" val="+v);
//		return v;
//	}
}
