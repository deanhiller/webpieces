package org.webpieces.webserver.impl;

import javax.inject.Inject;

import org.webpieces.ctx.api.WebConverter;
import org.webpieces.router.api.RouterService;
import org.webpieces.templating.api.ConverterLookup;

public class ConverterLookupProxy implements ConverterLookup {

	private RouterService router;

	@Inject
	public ConverterLookupProxy(RouterService router) {
		this.router = router;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public String convert(Object value) {
		if(value == null)
			return null;
		
		@SuppressWarnings("rawtypes")
		WebConverter converter = router.getConverter(value.getClass());
		if(converter != null)
			return converter.objectToString(value);
		return value+"";
	}

}
