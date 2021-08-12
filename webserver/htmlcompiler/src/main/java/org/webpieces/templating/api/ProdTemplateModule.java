package org.webpieces.templating.api;

import com.google.inject.Binder;
import com.google.inject.Module;

public class ProdTemplateModule implements Module {

	public static final String ROUTE_META_FILE = "org.webpieces.routeId.txt";
	public static final String ROUTE_TYPE = "route";
	public static final String PATH_TYPE = "path";
			
	private TemplateConfig config;

	public ProdTemplateModule(TemplateConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(TemplateConfig.class).toInstance(config);
	}
}
