package org.webpieces.plugins.hibernate;

import org.webpieces.plugins.hibernate.app.ServiceToFail;
import org.webpieces.plugins.hibernate.app.ServiceToFailMock;

import com.google.inject.Binder;
import com.google.inject.Module;

class TestModule implements Module {
	private ServiceToFailMock mock;
	public TestModule() {
		this(new ServiceToFailMock());
	}

	public TestModule(ServiceToFailMock mock) {
		this.mock = mock;
		
	}
	@Override
	public void configure(Binder binder) {
		binder.bind(ServiceToFail.class).toInstance(mock);
	}
}