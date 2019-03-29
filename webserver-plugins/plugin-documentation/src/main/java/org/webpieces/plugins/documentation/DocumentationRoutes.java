package org.webpieces.plugins.documentation;

import static org.webpieces.router.api.routing.Port.BOTH;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routing.ScopedRoutes;
import org.webpieces.router.impl.model.bldr.RouteBuilder;
import org.webpieces.router.impl.model.bldr.ScopedRouteBuilder;

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
    protected void configure(RouteBuilder baseRouter, ScopedRouteBuilder scopedRouter) {
		scopedRouter.addRoute(BOTH, HttpMethod.GET,  "", "DocumentationController.mainDocs", DocumentationRouteId.MAIN_DOCS);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/routes", "DocumentationController.routes", DocumentationRouteId.ROUTES);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/controllers", "DocumentationController.controllers", DocumentationRouteId.CONTROLLERS);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/templates", "DocumentationController.templates", DocumentationRouteId.TEMPLATES);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/design", "DocumentationController.design", DocumentationRouteId.DESIGN);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/quickstart", "DocumentationController.quickStart", DocumentationRouteId.QUICK_START);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/quickstart2", "DocumentationController.quickStart2", DocumentationRouteId.QUICK_START2);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/quickstart3", "DocumentationController.quickStart3", DocumentationRouteId.QUICK_START3);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/quickstart4", "DocumentationController.quickStart4", DocumentationRouteId.QUICK_START4);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/quickstart5", "DocumentationController.quickStart5", DocumentationRouteId.QUICK_START5);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/quickstart6", "DocumentationController.quickStart6", DocumentationRouteId.QUICK_START6);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/quickstart7", "DocumentationController.quickStart7", DocumentationRouteId.QUICK_START7);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/quickstart8", "DocumentationController.quickStart8", DocumentationRouteId.QUICK_START8);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/quickstart9", "DocumentationController.quickStart9", DocumentationRouteId.QUICK_START9);

		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/easyupgrade", "DocumentationController.easyUpgrade", DocumentationRouteId.EASY_UPGRADE);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/extensionPoints", "DocumentationController.extensionPoints", DocumentationRouteId.EXTENSION_POINTS);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/randomfeatures", "DocumentationController.randomFeatures", DocumentationRouteId.RANDOM_FEATURES);

		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/plugins", "plugins/PluginController.plugins", DocumentationRouteId.PLUGINS);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/plugin/jackson", "plugins/PluginController.jacksonPlugin", DocumentationRouteId.JACKSON_PLUGIN);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/plugin/hibernate", "plugins/PluginController.hibernatePlugin", DocumentationRouteId.HIBERNATE_PLUGIN);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/plugin/backend", "plugins/PluginController.backendPlugin", DocumentationRouteId.BACKEND_PLUGIN);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/plugin/h2", "plugins/PluginController.h2Plugin", DocumentationRouteId.H2_PLUGIN);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/plugin/installSsl", "plugins/PluginController.installSslPlugin", DocumentationRouteId.INSTALL_SSL_PLUGIN);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/plugin/documentation", "plugins/PluginController.documentationPlugin", DocumentationRouteId.DOCUMENTATION_PLUGIN);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/plugin/properties", "plugins/PluginController.propertiesPlugin", DocumentationRouteId.PROPERTIES_PLUGIN);
		scopedRouter.addRoute(BOTH, HttpMethod.GET, "/plugin/codeGeneration", "plugins/PluginController.codeGenPlugin", DocumentationRouteId.CODE_GEN_PLUGIN);

		//Because the html hardcode the url path here, we must use baseRouter and avoid whatever path
		//the user passed in :(
		baseRouter.addStaticDir(BOTH, "/org/webpieces/plugins/documentation/", "/org/webpieces/plugins/documentation/", true);		
    }

	@Override
	protected String getScope() {
		return path;
	}

}
