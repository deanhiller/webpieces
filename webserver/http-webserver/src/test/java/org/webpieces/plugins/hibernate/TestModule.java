package org.webpieces.plugins.hibernate;

import org.webpieces.plugins.hibernate.app.ServiceToFail;
import org.webpieces.plugins.hibernate.app.ServiceToFailMock;
import org.webpieces.plugins.hibernate.app.dbo.LevelEducation;
import org.webpieces.plugins.hibernate.app.dbo.Role;
import org.webpieces.router.api.ObjectStringConverter;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

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
		
		@SuppressWarnings("rawtypes")
		Multibinder<ObjectStringConverter> conversionBinder = Multibinder.newSetBinder(binder, ObjectStringConverter.class);
		conversionBinder.addBinding().to(LevelEducation.WebConverter.class);
		conversionBinder.addBinding().to(Role.WebConverter.class);
	}
}