package org.webpieces.util.futures;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TestFutures {

	@Test
	public void testSetResultBeforeFunction() {
		PromiseImpl<Integer, Throwable> op = new PromiseImpl<>(null);
		op.setResult(5);
		
		final List<Integer> values = new ArrayList<>();
		Future<Integer, Throwable> future = op;
		future.setResultFunction(p -> values.add(p));
		
		Assert.assertEquals(5, values.get(0).intValue());
		
		op.setResult(85);
		
		Assert.assertEquals(1, values.size());
	}

	@Test
	public void testSetResultAfterFunction() {
		PromiseImpl<Integer, Throwable> op = new PromiseImpl<>(null);
		
		final List<Integer> values = new ArrayList<>();
		Future<Integer, Throwable> future = op;
		future.setResultFunction(p -> values.add(p));
		
		op.setResult(4);
		Assert.assertEquals(4, values.get(0).intValue());
		
		op.setResult(3);
		Assert.assertEquals(1, values.size());
	}
}
