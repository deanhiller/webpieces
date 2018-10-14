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
		path = config.getPluginPath();
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
		addRoute(HttpMethod.GET, "/quickstart4", "DocumentationController.quickStart4", DocumentationRouteId.QUICK_START4);
		addRoute(HttpMethod.GET, "/quickstart5", "DocumentationController.quickStart5", DocumentationRouteId.QUICK_START5);
		addRoute(HttpMethod.GET, "/quickstart6", "DocumentationController.quickStart6", DocumentationRouteId.QUICK_START6);
		addRoute(HttpMethod.GET, "/quickstart7", "DocumentationController.quickStart7", DocumentationRouteId.QUICK_START7);
		addRoute(HttpMethod.GET, "/quickstart8", "DocumentationController.quickStart8", DocumentationRouteId.QUICK_START8);
		addRoute(HttpMethod.GET, "/quickstart9", "DocumentationController.quickStart9", DocumentationRouteId.QUICK_START9);

		addRoute(HttpMethod.GET, "/easyupgrade", "DocumentationController.easyUpgrade", DocumentationRouteId.EASY_UPGRADE);
		addRoute(HttpMethod.GET, "/extensionPoints", "DocumentationController.extensionPoints", DocumentationRouteId.EXTENSION_POINTS);
		addRoute(HttpMethod.GET, "/randomfeatures", "DocumentationController.randomFeatures", DocumentationRouteId.RANDOM_FEATURES);

		addRoute(HttpMethod.GET, "/plugins", "plugins/PluginController.plugins", DocumentationRouteId.PLUGINS);
		addRoute(HttpMethod.GET, "/plugin/jackson", "plugins/PluginController.jacksonPlugin", DocumentationRouteId.JACKSON_PLUGIN);
		addRoute(HttpMethod.GET, "/plugin/hibernate", "plugins/PluginController.hibernatePlugin", DocumentationRouteId.HIBERNATE_PLUGIN);
		addRoute(HttpMethod.GET, "/plugin/backend", "plugins/PluginController.backendPlugin", DocumentationRouteId.BACKEND_PLUGIN);
		addRoute(HttpMethod.GET, "/plugin/h2", "plugins/PluginController.h2Plugin", DocumentationRouteId.H2_PLUGIN);
		addRoute(HttpMethod.GET, "/plugin/installSsl", "plugins/PluginController.installSslPlugin", DocumentationRouteId.INSTALL_SSL_PLUGIN);
		addRoute(HttpMethod.GET, "/plugin/documentation", "plugins/PluginController.documentationPlugin", DocumentationRouteId.DOCUMENTATION_PLUGIN);
		addRoute(HttpMethod.GET, "/plugin/properties", "plugins/PluginController.propertiesPlugin", DocumentationRouteId.PROPERTIES_PLUGIN);
		addRoute(HttpMethod.GET, "/plugin/codeGeneration", "plugins/PluginController.codeGenPlugin", DocumentationRouteId.CODE_GEN_PLUGIN);

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
