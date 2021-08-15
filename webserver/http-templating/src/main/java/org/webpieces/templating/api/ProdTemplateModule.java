package org.webpieces.templating.api;

import com.google.inject.Binder;
import com.google.inject.Module;

public class ProdTemplateModule implements Module {

	public static final String ROUTE_META_FILE = ProdConstants.ROUTE_META_FILE;
	public static final String ROUTE_TYPE = ProdConstants.ROUTE_TYPE;
	public static final String PATH_TYPE = ProdConstants.PATH_TYPE;
			
	private TemplateConfig config;

	public ProdTemplateModule(TemplateConfig config) {
		this.config = config;
	}
	
	@Override
	public void configure(Binder binder) {
		binder.bind(TemplateConfig.class).toInstance(config);
	}
}
