package org.webpieces.plugins.fortesting;

import org.webpieces.nio.api.SSLConfiguration;
import org.webpieces.nio.api.SSLEngineFactory;
import org.webpieces.router.api.extensions.SimpleStorage;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.name.Names;

public class EmptyModule implements Module {

	@Override
	public void configure(Binder binder) {
		binder.bind(SimpleStorage.class).toInstance(new EmptyStorage());
		
		binder.bind(SSLEngineFactory.class).to(SSLEngineFactoryWebServerTesting.class);
		binder.bind(SSLEngineFactory.class).annotatedWith(Names.named(SSLConfiguration.BACKEND_SSL)).to(SSLEngineFactoryWebServerTesting.class);
	}

}
