package org.webpieces.plugins.json;

import org.codehaus.jackson.map.ObjectMapper;
import org.webpieces.router.api.BodyContentBinder;

import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;

public class JacksonModule extends AbstractModule {

	@Override
	protected void configure() {
		Multibinder<BodyContentBinder> uriBinder = Multibinder.newSetBinder(binder(), BodyContentBinder.class);
	    uriBinder.addBinding().to(JacksonLookup.class);

	    bind(ObjectMapper.class).toInstance(new ObjectMapper());
	    
	}

}
