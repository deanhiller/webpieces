package org.webpieces.util.futures;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.metrics.Monitoring;
import org.webpieces.util.SneakyThrow;
import org.webpieces.util.locking.TestPermitQueue;
import org.webpieces.util.threading.DirectExecutor;
import org.webpieces.util.threading.FutureExecutor;
import org.webpieces.util.threading.FutureExecutors;

import java.util.Map;
import java.util.concurrent.*;

public class TestLoggingFutures {

	static {
		Logging.setupMDCForLogging();
	}

	private static final Logger log = LoggerFactory.getLogger(TestLoggingFutures.class);

	private Monitoring m = new Monitoring(new SimpleMeterRegistry());
	private FutureExecutor exec = new FutureExecutors().create(m, 1, "test", true);
	private FutureExecutor direct = new FutureExecutors().createDirect();

	//@Test
	public void testRegisterTwoListenersToSameFuture() throws ExecutionException, InterruptedException, TimeoutException {

		log.info("no context set");

		MDC.put("txId", "deanTx");
		XFuture<Integer> future = methodCall(0);

		//try to screw up the context with a runnable that runs after the first one but before the second one
		//due to single threadedness, this runs after first and before second
		exec.executeRunnable(new SimulateTwo(), null);

		future.thenAccept(f -> fire2(f));
		future.thenAccept(f -> fire(f+2));
		future.thenAccept(f -> fire(f + 3));
		XFuture<Object> temp = future.thenApply(f -> fire(f));
		XFuture<Integer> next = temp.thenCompose((f) -> methodCall(2));
		XFuture<Void> last = next.thenAccept(f -> fire(f));
		XFuture<String> handle = next.handle((r1, e) -> handle(r1, e));
		next.exceptionally((e) -> exception(e));

		MDC.remove("txId");

		log.info("run in middle but context is 0 now");
		last.get(20, TimeUnit.SECONDS);
	}

	private Integer exception(Throwable e) {
		log.info("exception");
		return 0;
	}

	public String handle(Integer resp, Throwable e) {
		log.info("handle");
		return "";
	}


	private XFuture<Integer> methodCall(int value) {

		if(value == 0)
			MDC.put("txId2", "tx2");
		log.info("method call going out"+value);
		XFuture<Integer> future = new XFuture<>();
		Runnable r = new MyRunnable(future, value);
		exec.executeRunnable(r, null);

		if(value == 0)
			MDC.remove("txId2");
		else
			throw new RuntimeException("exc");

		return future;
	}

	private class MyRunnable implements Runnable {
		private XFuture<Integer> future;
		private int value;

		public MyRunnable(XFuture<Integer> future, int value) {
			this.future = future;
			this.value = value;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(3000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			log.info("response coming back "+value);
			future.complete(value+1);
		}
	}

	private class SimulateTwo implements Runnable {

		@Override
		public void run() {
			MDC.put("txId", "another1");
			MDC.put("txId2", "another2");

			log.info("simulate runnable 2");
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}

			log.info("simulate runnable 2 DONE");

			MDC.remove("txId2");
			//leak the txId1 like a developer might do ;)
		}
	}

	private Object fire2(Integer f) {
		log.info("fire2="+f);
		return null;
	}

	private Object fire(Integer f) {
		log.info("f="+f);
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

}
