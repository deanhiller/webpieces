package webpiecesxxxxxpackage.mock;

import java.util.function.Supplier;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;

import webpiecesxxxxxpackage.service.SomeLibrary;

public class MockSomeLibrary extends MockSuperclass implements SomeLibrary {

	public static enum Method implements MethodEnum {
		DO_SOMETHING
	}
	
	@Override
	public void doSomething(int something) {
		super.calledMethod(Method.DO_SOMETHING, something);
	}

	public void addExceptionToThrow(Supplier<Void> exc) {
		super.addExceptionToThrow(Method.DO_SOMETHING, exc);
	}

}
