package org.webpieces.plugins.documentation;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.ScopedRoutes;

public class DocumentationRoutes extends ScopedRoutes {
	
    public static final String HOME_PATH = "";
	public static final String TEMPLATES_PATH = "/templates";
	public static final String ROUTES_PATH = "/routes";
	
	private String path;

	public DocumentationRoutes(DocumentationConfig config) {
		super();
		path = config.getDocumentationPath();
	}

    @Override
    protected void configure() {
		addRoute(HttpMethod.GET,  "", "DocumentationController.mainDocs", DocumentationRouteId.MAIN_DOCS);
		addRoute(HttpMethod.GET, "/routes", "DocumentationController.routes", DocumentationRouteId.ROUTES);
		addRoute(HttpMethod.GET, "/controllers", "DocumentationController.controllers", DocumentationRouteId.CONTROLLERS);
		addRoute(HttpMethod.GET, "/templates", "DocumentationController.templates", DocumentationRouteId.TEMPLATES);
		addRoute(HttpMethod.GET, "/design", "DocumentationController.design", DocumentationRouteId.DESIGN);
		addRoute(HttpMethod.GET, "/quickstart", "DocumentationController.quickStart", DocumentationRouteId.QUICK_START);
		addRoute(HttpMethod.GET, "/quickstart2", "DocumentationController.quickStart2", DocumentationRouteId.QUICK_START2);
		addRoute(HttpMethod.GET, "/quickstart3", "DocumentationController.quickStart3", DocumentationRouteId.QUICK_START3);

		addRoute(HttpMethod.GET, "/randomfeatures", "DocumentationController.randomFeatures", DocumentationRouteId.RANDOM_FEATURES);
		
		//Because the html hardcode the url path here, we must use baseRouter and avoid whatever path
		//the user passed in :(
		baseRouter.addStaticDir("/org/webpieces/plugins/documentation/", "/org/webpieces/plugins/documentation/", true);		
    }

	@Override
	protected String getScope() {
		return path;
	}

	@Override
	protected boolean isHttpsOnlyRoutes() {
		return false;
	}
}
