package org.webpieces.router.impl;

import org.webpieces.router.api.BodyContentBinder;
import org.webpieces.router.api.EntityLookup;
import org.webpieces.router.api.ObjectStringConverter;
import org.webpieces.router.api.Startable;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

public class EmptyPluginModule implements Module {

	private RoutingHolder routingHolder;

	public EmptyPluginModule(RoutingHolder routingHolder) {
		this.routingHolder = routingHolder;
	}

	@Override
	public void configure(Binder binder) {
		//creates an empty binder in case app installs ZERO plugins
		Multibinder.newSetBinder(binder, Startable.class);
		
		Multibinder.newSetBinder(binder, EntityLookup.class);

		Multibinder.newSetBinder(binder, BodyContentBinder.class);
		
		Multibinder.newSetBinder(binder, ObjectStringConverter.class);		
		
		//special case so the notFound controller can inpsect and list all routes in a web page
		//OR some client application can inject and introspect all web routes as well
		//OR some plugin on startup can look at all routes as well
		binder.bind(RoutingHolder.class).toInstance(routingHolder);
	}

}
