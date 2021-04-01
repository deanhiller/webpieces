package org.webpieces.plugin.json;

import javax.inject.Scope;
import javax.inject.Singleton;

import org.webpieces.router.api.extensions.BodyContentBinder;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class JacksonModule extends AbstractModule {

	private JacksonConfig config;

	public JacksonModule(JacksonConfig config) {
		this.config = config;
	}
	
	@Override
	protected void configure() {
		Multibinder<BodyContentBinder> uriBinder = Multibinder.newSetBinder(binder(), BodyContentBinder.class);
	    uriBinder.addBinding().to(JacksonLookup.class);

	    ConverterConfig converterConfig = new ConverterConfig();
	    converterConfig.setConvertNullToEmptyStr(config.isConvertNullToEmptyStr());
	    
	    bind(ObjectMapper.class).toProvider(ObjectMapperFactory.class).in(Singleton.class);
	    bind(JacksonConfig.class).toInstance(config);
	    bind(ConverterConfig.class).toInstance(converterConfig);
	}

}
