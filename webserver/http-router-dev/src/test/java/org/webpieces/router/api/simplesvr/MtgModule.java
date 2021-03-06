package org.webpieces.router.api.simplesvr;

import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.impl.ApplicationContextImpl;

import com.google.inject.Binder;
import com.google.inject.Module;

public class MtgModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(SomeUtil.class).to(SomeUtilImpl.class);
		binder.bind(SimpleStorage.class).toInstance(new EmptyStorage());
		binder.bind(ApplicationContext.class).to(ApplicationContextImpl.class).asEagerSingleton();
	}

}
