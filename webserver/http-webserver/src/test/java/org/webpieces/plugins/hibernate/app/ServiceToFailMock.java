package org.webpieces.plugins.hibernate.app;

import org.webpieces.mock.MethodEnum;
import org.webpieces.mock.MockSuperclass;
import org.webpieces.mock.ParametersPassedIn;

import java.util.List;
import java.util.function.Supplier;

public class ServiceToFailMock extends MockSuperclass implements ServiceToFail {

	private enum Method implements MethodEnum {
		FAIL
	}
	
	@Override
	public void fail(int userIdToLoad) {
		super.calledVoidMethod(Method.FAIL, userIdToLoad);
	}
	
	public void addException(Supplier<?> exc) {
		super.addExceptionToThrow(Method.FAIL, exc);
	}
	
	public int getLastUserId() {
		List<ParametersPassedIn> list = super.getCalledMethodList(Method.FAIL);
		Integer val = (Integer)list.get(0).getArgs()[0];
		return val;
	}

}
