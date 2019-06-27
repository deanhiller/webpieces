package org.webpieces.plugins.documentation;

import static org.webpieces.router.api.routes.Port.BOTH;

import org.webpieces.ctx.api.HttpMethod;
import org.webpieces.router.api.routebldr.DomainRouteBuilder;
import org.webpieces.router.api.routebldr.RouteBuilder;
import org.webpieces.router.api.routebldr.ScopedRouteBuilder;
import org.webpieces.router.api.routes.Routes;

public class DocumentationRoutes implements Routes {
	
    public static final String HOME_PATH = "";
	public static final String TEMPLATES_PATH = "/templates";
	public static final String ROUTES_PATH = "/routes";
	
	private String path;

	public DocumentationRoutes(DocumentationConfig config) {
		super();
		path = config.getPluginPath();
	}

	@Override
	public void configure(DomainRouteBuilder domainRouteBldr) {
		RouteBuilder baseBldr = domainRouteBldr.getBackendRouteBuilder();
		ScopedRouteBuilder scopedBldr = baseBldr.getScopedRouteBuilder(path);
		
		scopedBldr.addRoute(BOTH, HttpMethod.GET,  "", "DocumentationController.mainDocs", DocumentationRouteId.MAIN_DOCS);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/html", "DocumentationController.html", DocumentationRouteId.HTML_REFERENCE);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/routes", "DocumentationController.routes", DocumentationRouteId.ROUTES);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/controllers", "DocumentationController.controllers", DocumentationRouteId.CONTROLLERS);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/templates", "DocumentationController.templates", DocumentationRouteId.TEMPLATES);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/design", "DocumentationController.design", DocumentationRouteId.DESIGN);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/quickstart", "DocumentationController.quickStart", DocumentationRouteId.QUICK_START);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/quickstart2", "DocumentationController.quickStart2", DocumentationRouteId.QUICK_START2);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/quickstart3", "DocumentationController.quickStart3", DocumentationRouteId.QUICK_START3);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/quickstart4", "DocumentationController.quickStart4", DocumentationRouteId.QUICK_START4);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/quickstart5", "DocumentationController.quickStart5", DocumentationRouteId.QUICK_START5);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/quickstart6", "DocumentationController.quickStart6", DocumentationRouteId.QUICK_START6);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/quickstart7", "DocumentationController.quickStart7", DocumentationRouteId.QUICK_START7);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/quickstart8", "DocumentationController.quickStart8", DocumentationRouteId.QUICK_START8);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/quickstart9", "DocumentationController.quickStart9", DocumentationRouteId.QUICK_START9);

		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/easyupgrade", "DocumentationController.easyUpgrade", DocumentationRouteId.EASY_UPGRADE);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/extensionPoints", "DocumentationController.extensionPoints", DocumentationRouteId.EXTENSION_POINTS);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/randomfeatures", "DocumentationController.randomFeatures", DocumentationRouteId.RANDOM_FEATURES);

		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/plugins", "plugins/PluginController.plugins", DocumentationRouteId.PLUGINS);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/plugin/jackson", "plugins/PluginController.jacksonPlugin", DocumentationRouteId.JACKSON_PLUGIN);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/plugin/hibernate", "plugins/PluginController.hibernatePlugin", DocumentationRouteId.HIBERNATE_PLUGIN);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/plugin/backend", "plugins/PluginController.backendPlugin", DocumentationRouteId.BACKEND_PLUGIN);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/plugin/h2", "plugins/PluginController.h2Plugin", DocumentationRouteId.H2_PLUGIN);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/plugin/installSsl", "plugins/PluginController.installSslPlugin", DocumentationRouteId.INSTALL_SSL_PLUGIN);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/plugin/documentation", "plugins/PluginController.documentationPlugin", DocumentationRouteId.DOCUMENTATION_PLUGIN);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/plugin/properties", "plugins/PluginController.propertiesPlugin", DocumentationRouteId.PROPERTIES_PLUGIN);
		scopedBldr.addRoute(BOTH, HttpMethod.GET, "/plugin/codeGeneration", "plugins/PluginController.codeGenPlugin", DocumentationRouteId.CODE_GEN_PLUGIN);

		//Because the html hardcode the url path here, we must use baseRouter and avoid whatever path
		//the user passed in :(
		baseBldr.addStaticDir(BOTH, "/org/webpieces/plugins/documentation/", "/org/webpieces/plugins/documentation/", true);		
    }

}
