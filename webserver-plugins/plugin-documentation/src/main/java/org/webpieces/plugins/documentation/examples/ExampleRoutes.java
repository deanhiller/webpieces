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
		
		scopedBldr.addRoute(BOTH, HttpMethod.GET , "/examples/checkbox", "ExamplesController.checkbox", ExampleRouteId.CHECKBOX);
		scopedBldr.addRoute(BOTH, HttpMethod.POST, "/examples/postCheckbox", "ExamplesController.postCheckbox", ExampleRouteId.POST_CHECKBOX);
		scopedBldr.addRoute(BOTH, HttpMethod.GET , "/examples/checkboxResult", "ExamplesController.checkboxResult", ExampleRouteId.CHECKBOX_RESULT);

		scopedBldr.addRoute(BOTH, HttpMethod.GET , "/examples/enumList", "ExamplesController.enumList", ExampleRouteId.ENUM_LIST_SINGLE_SELECT);
		scopedBldr.addRoute(BOTH, HttpMethod.POST, "/examples/postEnumList", "ExamplesController.postEnumList", ExampleRouteId.POST_ENUM_LIST_SINGLE_SELECT);
		scopedBldr.addRoute(BOTH, HttpMethod.GET , "/examples/enumListResult", "ExamplesController.enumListResult", ExampleRouteId.ENUM_LIST_SINGLE_SELECT_RESULT);

    }
}
