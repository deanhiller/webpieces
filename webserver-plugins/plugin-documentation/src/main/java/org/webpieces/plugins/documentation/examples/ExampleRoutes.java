package org.webpieces.plugins.documentation.examples;

import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.plugins.documentation.DocumentationConfig;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.ScopedRoutes;

public class ExampleRoutes extends ScopedRoutes {

	private String path;

	public ExampleRoutes(DocumentationConfig config) {
		path = config.getPluginPath();
	}
	
	@Override
	protected String getScope() {
		return path;
	}


    @Override
    protected void configure(RouteBuilder baseBldr, ScopedRouteBuilder scopedBldr) {
    	//The GET/POST Routes
		scopedBldr.addRoute(BOTH, HttpMethod.GET , "/examples/input", "ExamplesController.inputText", ExampleRouteId.INPUT_TEXT);
		scopedBldr.addRoute(BOTH, HttpMethod.POST, "/examples/postInput", "ExamplesController.postInputText", ExampleRouteId.POST_INPUT_TEXT_FORM);
		scopedBldr.addRoute(BOTH, HttpMethod.GET , "/examples/inputResult", "ExamplesController.inputTextResult", ExampleRouteId.INPUT_TEXT_RESULT);
    }
}
