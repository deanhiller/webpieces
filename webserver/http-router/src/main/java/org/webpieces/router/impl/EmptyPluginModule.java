package org.webpieces.router.impl;

import org.webpieces.router.api.BodyContentBinder;
import org.webpieces.router.api.EntityLookup;
import org.webpieces.router.api.ObjectStringConverter;
import org.webpieces.router.api.Startable;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

public class EmptyPluginModule implements Module {

	@Override
	public void configure(Binder binder) {
		//creates an empty binder in case app installs ZERO plugins
		Multibinder.newSetBinder(binder, Startable.class);
		
		Multibinder.newSetBinder(binder, EntityLookup.class);

		Multibinder.newSetBinder(binder, BodyContentBinder.class);
		
		Multibinder.newSetBinder(binder, ObjectStringConverter.class);		
	}

}
