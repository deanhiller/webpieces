package org.webpieces.javasm.api;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.webpieces.javasm.api.TestStateMachine.FakeInterface;

public class MockFakeInterface implements FakeInterface {

	private List<String> methodsCalled = new ArrayList<>();
	
	@Override
	public void first() {
		methodsCalled.add("first");
	}

	@Override
	public void second() {
		methodsCalled.add("second");
	}

	public void expectCalls(String method1, String method2) {
		Assert.assertEquals(2, methodsCalled.size());
		
		Assert.assertEquals(method1, methodsCalled.get(0));
		Assert.assertEquals(method2, methodsCalled.get(1));
	}

	
}
