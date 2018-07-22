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
		addRoute(HttpMethod.GET, "/templates", "DocumentationController.templates", DocumentationRouteId.TEMPLATES);
		addRoute(HttpMethod.GET, "/routes", "DocumentationController.routes", DocumentationRouteId.ROUTES);

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
