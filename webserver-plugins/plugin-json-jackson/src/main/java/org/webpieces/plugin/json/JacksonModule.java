package org.webpieces.plugin.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.webpieces.router.api.extensions.BodyContentBinder;

import javax.inject.Singleton;

public class JacksonModule extends AbstractModule {

	private JacksonConfig config;

	public JacksonModule(JacksonConfig config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		Multibinder<BodyContentBinder> uriBinder = Multibinder.newSetBinder(binder(), BodyContentBinder.class);
	    uriBinder.addBinding().to(JacksonLookup.class);

	    ConverterConfig converterConfig = new ConverterConfig(config.isConvertNullToEmptyStr());

	    bind(ObjectMapper.class).toProvider(ObjectMapperProvider.class).in(Singleton.class);
	    bind(JacksonConfig.class).toInstance(config);
	    bind(ConverterConfig.class).toInstance(converterConfig);
	}

}
