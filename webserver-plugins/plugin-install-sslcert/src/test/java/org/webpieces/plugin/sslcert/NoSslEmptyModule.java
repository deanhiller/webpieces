package org.webpieces.plugin.sslcert;

import org.webpieces.ctx.api.ApplicationContext;
import org.webpieces.plugins.fortesting.EmptyStorage;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.impl.ApplicationContextImpl;

import com.google.inject.Binder;
import com.google.inject.Module;

public class NoSslEmptyModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(SimpleStorage.class).toInstance(new EmptyStorage());
		binder.bind(ApplicationContext.class).toInstance(new ApplicationContextImpl());
	}

}
