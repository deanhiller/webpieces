package org.webpieces.router.impl;

import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.ctx.api.extension.HtmlTagCreator;
import org.webpieces.router.api.PlatformInjector;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.api.extensions.EntityLookup;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.api.extensions.Startable;
import org.webpieces.router.impl.mgmt.ManagedBeanMeta;
import org.webpieces.router.impl.params.ObjectTranslator;

import com.google.inject.Binder;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

import io.micrometer.core.instrument.MeterRegistry;
import org.webpieces.util.context.PlatformHeaders;

public class WebpiecesToAppBindingModule implements Module {

	private RoutingHolder routingHolder;
	private ManagedBeanMeta managedMeta;
	private ObjectTranslator objectTranslator;
	private ScheduledExecutorService scheduler;
	private MeterRegistry metrics;
	private Injector injector;

	public WebpiecesToAppBindingModule(
			RoutingHolder routingHolder, 
			ManagedBeanMeta managedMeta, 
			Injector injector,
			ObjectTranslator objectTranslator, 
			ScheduledExecutorService scheduler, 
			MeterRegistry metrics
	) {
		this.routingHolder = routingHolder;
		this.managedMeta = managedMeta;
		this.injector = injector;
		this.objectTranslator = objectTranslator;
		this.scheduler = scheduler;
		this.metrics = metrics;
	}

	@Override
	public void configure(Binder binder) {
		//creates an empty binder in case app installs ZERO plugins
		Multibinder.newSetBinder(binder, Startable.class);
		Multibinder.newSetBinder(binder, EntityLookup.class);
		Multibinder.newSetBinder(binder, BodyContentBinder.class);
		Multibinder.newSetBinder(binder, ObjectStringConverter.class);
		Multibinder.newSetBinder(binder, HtmlTagCreator.class);
		Multibinder.newSetBinder(binder, PlatformHeaders.class);

		//special case so the notFound controller can inspect and list all routes in a web page
		//OR some client application can inject and introspect all web routes as well
		//OR some plugin on startup can look at all routes as well
		//Also, special case so the backend plugin can accept route ids and reverse lookup the URL on them
		binder.bind(RoutingHolder.class).toInstance(routingHolder);

		//These next two bindings are specifically used by the properties plugin but could be used by any other plugins as well
		
		//special case exposing webpieces managed beans for modification to any plugin that wants to use it
		binder.bind(ManagedBeanMeta.class).toInstance(managedMeta);
		//special case exposing webpieces + application converters (object to string and string to object)
		//This is the lookup class for all those ObjectStringConverter that get installed in the above Multibinder
		//including if you put an object into a cookie as well, so it gets written as a String
		binder.bind(ObjectTranslator.class).toInstance(objectTranslator);
		binder.bind(ScheduledExecutorService.class).toInstance(scheduler);
		
		binder.bind(PlatformInjector.class).toInstance(new PlatformInjector(injector));
		
		binder.bind(MeterRegistry.class).toInstance(metrics);
	}
}
