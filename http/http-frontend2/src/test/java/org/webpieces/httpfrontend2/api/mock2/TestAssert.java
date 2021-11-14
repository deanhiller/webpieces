package org.webpieces.httpfrontend2.api.mock2;

import org.webpieces.util.futures.XFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Assert;

public class TestAssert {

	public static <T> Throwable intercept(XFuture<T> future) {
		try {
			future.get(10, TimeUnit.SECONDS);
			Assert.fail("Should have thrown an exception and did not");
			throw new RuntimeException("should be impossible1");
		} catch (InterruptedException e) {
			throw new RuntimeException("should be impossible2");
		} catch (ExecutionException e) {
			return e.getCause();
		} catch (TimeoutException e) {
			Assert.fail("The future never completed");
			throw new RuntimeException("should be impossible2");			
		}
	}
}
