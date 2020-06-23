package org.webpieces.plugin.dto;

import org.webpieces.router.api.extensions.EntityLookup;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class DtoModule extends AbstractModule {

	public DtoModule(DtoConfiguration config) {
	}
	
	@Override
	protected void configure() {
		Multibinder<EntityLookup> uriBinder = Multibinder.newSetBinder(binder(), EntityLookup.class);
	    uriBinder.addBinding().to(DtoLookup.class);
	}

}
