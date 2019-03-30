package org.webpieces.webserver.impl;

import javax.inject.Inject;

import org.webpieces.router.api.RouterService;
import org.webpieces.router.api.extensions.ObjectStringConverter;
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
		ObjectStringConverter converter = router.getConverterFor(value);
		return converter.objectToString(value);
	}

}
