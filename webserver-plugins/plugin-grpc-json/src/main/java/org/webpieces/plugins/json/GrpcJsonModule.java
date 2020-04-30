package org.webpieces.plugins.json;

import org.webpieces.router.api.extensions.BodyContentBinder;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class GrpcJsonModule extends AbstractModule {

	@Override
	protected void configure() {
		Multibinder<BodyContentBinder> uriBinder = Multibinder.newSetBinder(binder(), BodyContentBinder.class);
	    uriBinder.addBinding().to(GrpcJsonLookup.class);
	}

}
