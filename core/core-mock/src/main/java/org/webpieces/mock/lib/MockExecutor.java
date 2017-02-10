package org.webpieces.mock.lib;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

public class MockExecutor extends MockSuperclass implements Executor {

	private static enum Method implements MethodEnum {
		EXECUTE;
	}
	
	@Override
	public void execute(Runnable command) {
		super.calledVoidMethod(Method.EXECUTE, command);
	}

	public List<Runnable> getRunnablesScheduled() {
		Stream<ParametersPassedIn> calledMethods = super.getCalledMethods(Method.EXECUTE);
		Stream<Runnable> runnables = calledMethods.map(s -> (Runnable)s.getArgs()[0]);
		return runnables.collect(Collectors.toList());
	}

}
