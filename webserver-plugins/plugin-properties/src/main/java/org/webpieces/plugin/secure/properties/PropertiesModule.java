package org.webpieces.plugin.secure.properties;

import java.util.concurrent.ScheduledExecutorService;

import org.webpieces.plugin.backend.spi.BackendGuiDescriptor;
import org.webpieces.plugin.secure.properties.beans.BeanMetaData;
import org.webpieces.plugin.secure.properties.beans.GuiceTypeListener;
import org.webpieces.router.api.extensions.SimpleStorage;
import org.webpieces.router.api.extensions.Startable;
import org.webpieces.router.impl.mgmt.ManagedBeanMeta;
import org.webpieces.router.impl.params.ObjectTranslator;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.matcher.Matchers;
import com.google.inject.multibindings.Multibinder;

public class PropertiesModule implements Module {

	private PropertiesConfig config;

	public PropertiesModule(PropertiesConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		Multibinder<BackendGuiDescriptor> backendBinder = Multibinder.newSetBinder(binder, BackendGuiDescriptor.class);
	    backendBinder.addBinding().to(PropertiesGuiDescriptor.class);

	    binder.bind(PropertiesConfig.class).toInstance(config);

		Multibinder<Startable> startableBinder = Multibinder.newSetBinder(binder, Startable.class);
	    
	    //Unfortunately, DURING guice heirarchy construction, we need to record into BeanMetaData
	    //later, after Guice construction is complete and the Startable.class implementations are being
	    //called, BeanMetaData can officially use the ObjectTranslator at that point to know if it
	    //can install methods or not into the GUI
	    //
	    //This is a bit complex but 
	    //1. while guice is creating things, it records in BeanMetaData(which is not injected into anything except the guice listeners at this point)
	    //2. guice is done creating the full tree, webpieces invokes all Startable.class including BeanMetaData
	    //3. BeanMetaData's start() method now loads all methods and such
	    //4. BeanMetaData now loads all properties from DB and applies them from the DB if they exist
	    //5. BeanMetaData now kicks off a recurring task to load DB properties (in case props are edited on another server)
	    Provider<ObjectTranslator> translatorProvider = binder.getProvider(ObjectTranslator.class);
	    Provider<SimpleStorage> storageProvider = binder.getProvider(SimpleStorage.class);
	    Provider<ManagedBeanMeta> webpiecesBeanProvider = binder.getProvider(ManagedBeanMeta.class);
	    Provider<ScheduledExecutorService> schedulerProvider = binder.getProvider(ScheduledExecutorService.class);
	    
	    BeanMetaData proxy = new BeanMetaData(config, translatorProvider, storageProvider, webpiecesBeanProvider, schedulerProvider);
	    //this binding is for the controller....
	    binder.bind(BeanMetaData.class).toInstance(proxy);
	    //this binding is to plugin to webpieces so the start() method is called 
	    startableBinder.addBinding().toInstance(proxy);
	    
	    binder.bindListener(Matchers.any(), new GuiceTypeListener(proxy, config));
	}

}
