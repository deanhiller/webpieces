package org.webpieces.plugins.hibernate;

import org.webpieces.ctx.api.WebConverter;
import org.webpieces.plugins.hibernate.app.ServiceToFail;
import org.webpieces.plugins.hibernate.app.ServiceToFailMock;
import org.webpieces.plugins.hibernate.app.dbo.LevelEducation;

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
		Multibinder<WebConverter> conversionBinder = Multibinder.newSetBinder(binder, WebConverter.class);
		conversionBinder.addBinding().to(LevelEducation.EnumStringConverter.class);
	}
}