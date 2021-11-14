package org.webpieces.util.filters;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Assert;
import org.junit.Test;
import org.webpieces.util.futures.FutureHelper;


public class TestFilters {

	/**
	 * Filter demonstration.  Could make more advanced but this works great and I prefer KISS so
	 * others that read it can understand it easier as it is already more complicated than I wish.
	 * @throws ExecutionException 
	 * @throws InterruptedException 
	 */
	@Test
	public void testFilters() throws InterruptedException, ExecutionException {
		Filter.setFutureUtil(new FutureHelper());
		MyFilter filterMiddle = new MyFilter("middle");
		MyFilter filterTop = new MyFilter("top");
		
		Service<Integer, String> service2 = filterTop
												.chain(filterMiddle)
												.chain(new SomeService());
		
		Service<Integer, String> x3 = filterMiddle
											.chain(filterTop)
											.chain(new SomeService());
		
		XFuture<String> future = service2.invoke(5);
		String result = future.get();
		Assert.assertEquals("top", result);
		
		XFuture<String> future2 = x3.invoke(2);
		String result2 = future2.get();
		Assert.assertEquals("middle", result2);
	}
}
