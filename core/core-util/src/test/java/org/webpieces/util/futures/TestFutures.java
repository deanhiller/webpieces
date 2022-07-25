package org.webpieces.util.futures;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;

import org.digitalforge.sneakythrow.SneakyThrow;

public class TestFutures {

	//private static final Logger log = LoggerFactory.getLogger(TestFutures.class);

	@Test
	public void testRegisterTwoListenersToSameFuture() {
		XFuture<Integer> future = new XFuture<Integer>();

		
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
		XFuture<Integer> future = writeData(counter);
		future.thenAccept(p -> recurse(p+1)).join();		
	}
	
	private XFuture<Integer> writeData(int counter) {
		System.out.println("counter="+counter);
		if(counter == 1455) {
			System.out.println("mark");
		}
		
		XFuture<Integer> future = new XFuture<Integer>();
		future.complete(counter);
		return future;
	}
	
	@Test
	public void testXFuture() throws InterruptedException {
		System.out.println("thread="+Thread.currentThread());
		XFuture<Integer> myFuture = new XFuture<>();
		myFuture.thenAccept(intResult -> log(intResult));
		XFuture<String> result = myFuture.thenApply(p -> translate(p));
		
		result.handle((r, e) -> handle(r, e));
		
		result.cancel(true);
		
		myFuture.complete(8);
		
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
	public void testAFinallyMethodWithXFutures() throws InterruptedException, ExecutionException {
		XFuture<Integer> myFuture = new XFuture<>();
		XFuture<Integer> newFuture = myFuture.thenApply(intResult -> throwException());
		
		myFuture.complete(5);

		Assert.assertFalse(myFuture.isCompletedExceptionally());
		Assert.assertTrue(newFuture.isCompletedExceptionally());
		
		XFuture<Integer> future2 = newFuture.handle((r, e) -> {
			if(r != null)
				return r;
			else if(e != null)
				throw SneakyThrow.sneak(e);
			else
				throw new RuntimeException("weird");
		});
		
		Assert.assertTrue(future2.isCompletedExceptionally());
		
		
		XFuture<Object> future = newFuture.handle((r, e) -> {
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
//		XFuture<Integer> future = new XFuture<Integer>();
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
