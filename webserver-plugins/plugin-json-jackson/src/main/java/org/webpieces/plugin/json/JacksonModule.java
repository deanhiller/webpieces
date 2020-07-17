package org.webpieces.plugin.json;

import org.webpieces.router.api.extensions.BodyContentBinder;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import com.fasterxml.jackson.databind.ObjectMapper;
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

	    ObjectMapper mapper = new ObjectMapper();
	    
	    if(config.isConvertNullToEmptyStr()) {
	        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
	        mapper.configOverride(String.class)
	                .setSetterInfo(JsonSetter.Value.forValueNulls(Nulls.AS_EMPTY));
	    }
	    
	    ConverterConfig converterConfig = new ConverterConfig();
	    converterConfig.setConvertNullToEmptyStr(config.isConvertNullToEmptyStr());
	    
	    bind(ObjectMapper.class).toInstance(mapper);
	    bind(JacksonConfig.class).toInstance(config);
	    bind(ConverterConfig.class).toInstance(converterConfig);
	}

}
