package org.webpieces.router.impl;

import java.util.Set;

import javax.inject.Inject;
import javax.inject.Provider;
import javax.inject.Singleton;

import org.webpieces.ctx.api.extension.HtmlTagCreator;
import org.webpieces.router.api.TemplateApi;
import org.webpieces.router.api.extensions.BodyContentBinder;
import org.webpieces.router.api.extensions.EntityLookup;
import org.webpieces.router.api.extensions.ObjectStringConverter;
import org.webpieces.router.impl.model.BodyContentBinderChecker;
import org.webpieces.router.impl.params.ObjectTranslator;
import org.webpieces.router.impl.params.ParamToObjectTranslatorImpl;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;

@Singleton
public class PluginSetup {

	private ParamToObjectTranslatorImpl translator;
	private ObjectTranslator translation;
	private BodyContentBinderChecker bodyContentChecker;
	private Provider<TemplateApi> templateApi;

	@Inject
	public PluginSetup(
			ParamToObjectTranslatorImpl translator, 
			BodyContentBinderChecker bodyContentChecker,
			ObjectTranslator translation,
			Provider<TemplateApi> templateApi
	) {
		this.translator = translator;
		this.bodyContentChecker = bodyContentChecker;
		this.translation = translation;
		this.templateApi = templateApi;
	}

	/**
	 * This is where we wire in all plugin points EXCEPT the Startup one
	 * we can't inject them :( 
	 */
	@SuppressWarnings("rawtypes")
	public void wireInPluginPoints(Injector appInjector) {

		Key<Set<EntityLookup>> key = Key.get(new TypeLiteral<Set<EntityLookup>>(){});
		Set<EntityLookup> lookupHooks = appInjector.getInstance(key);
		translator.install(lookupHooks);
		
		Key<Set<ObjectStringConverter>> key3 = Key.get(new TypeLiteral<Set<ObjectStringConverter>>(){});
		Set<ObjectStringConverter> converters = appInjector.getInstance(key3);
		translation.install(converters);

		Key<Set<BodyContentBinder>> key2 = Key.get(new TypeLiteral<Set<BodyContentBinder>>(){});
		Set<BodyContentBinder> bodyBinders = appInjector.getInstance(key2);
		bodyContentChecker.install(bodyBinders);
		
		Key<Set<HtmlTagCreator>> key4 = Key.get(new TypeLiteral<Set<HtmlTagCreator>>() {});
		Set<HtmlTagCreator> htmlTagCreators = appInjector.getInstance(key4);
		
		//Guice circular dependency we could not work around quite yet.  figure out later maybe
		TemplateApi api = templateApi.get();
		api.installCustomTags(htmlTagCreators);
	}

}
