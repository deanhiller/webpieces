package org.webpieces.plugin.documentation.examples;

import org.webpieces.router.api.extensions.ObjectStringConverter;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.multibindings.Multibinder;

public class ExampleGuice implements Module {


	@SuppressWarnings("rawtypes")
	@Override
	public void configure(Binder binder) {
		Multibinder<ObjectStringConverter> conversionBinder = Multibinder.newSetBinder(binder, ObjectStringConverter.class);
		conversionBinder.addBinding().to(ColorEnum.WebConverter.class);
		
	}
}
