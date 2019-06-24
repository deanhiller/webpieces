package org.webpieces.plugins.documentation;

import java.util.List;

import org.webpieces.plugins.documentation.examples.ExampleRoutes;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class WebpiecesDocumentationPlugin implements Plugin {

	private DocumentationConfig config;

	public WebpiecesDocumentationPlugin(DocumentationConfig config) {
		super();
		this.config = config;
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new DocumentationModule(config));
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(
			new DocumentationRoutes(config),
			new ExampleRoutes(config)
		);
	}

}
