package org.webpieces.router.impl;

import java.util.Set;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.webpieces.router.api.BodyContentBinder;
import org.webpieces.router.api.EntityLookup;
import org.webpieces.router.impl.loader.MetaLoader;
import org.webpieces.router.impl.params.ParamToObjectTranslatorImpl;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

@Singleton
public class PluginSetup {

	private ParamToObjectTranslatorImpl translator;
	private MetaLoader loader;

	@Inject
	public PluginSetup(ParamToObjectTranslatorImpl translator, MetaLoader loader) {
		this.translator = translator;
		this.loader = loader;
	}

	/**
	 * This is where we wire in all plugin points EXCEPT the Startup one
	 * we can't inject them 
	 */
	public void wireInPluginPoints(Injector appInjector, Consumer<Injector> startupFunction) {

		Key<Set<EntityLookup>> key = Key.get(new TypeLiteral<Set<EntityLookup>>(){});
		Set<EntityLookup> lookupHooks = appInjector.getInstance(key);
		
		Key<Set<BodyContentBinder>> key2 = Key.get(new TypeLiteral<Set<BodyContentBinder>>(){});
		Set<BodyContentBinder> bodyBinders = appInjector.getInstance(key2);

		translator.install(lookupHooks, bodyBinders);
		loader.install(bodyBinders);
		
		//wire in startup and start the startables.  This is a function since Dev and Production differ
		//in that Development we have to make sure we don't run startup code twice as it is likely to
		//blow up....or should we make this configurable?
		startupFunction.accept(appInjector);
	}

}
