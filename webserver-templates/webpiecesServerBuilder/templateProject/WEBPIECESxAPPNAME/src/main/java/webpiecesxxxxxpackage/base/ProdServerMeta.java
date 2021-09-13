package webpiecesxxxxxpackage.base;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.microsvc.server.api.RESTApiRoutes;
import org.webpieces.plugin.backend.BackendPlugin;
import org.webpieces.plugin.hibernate.HibernatePlugin;
import org.webpieces.plugin.json.JacksonConfig;
import org.webpieces.plugin.json.JacksonPlugin;
import org.webpieces.plugin.secure.properties.PropertiesConfig;
import org.webpieces.plugin.secure.properties.PropertiesPlugin;
import org.webpieces.plugin.secure.sslcert.InstallSslCertConfig;
import org.webpieces.plugin.secure.sslcert.InstallSslCertPlugin;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

import webpiecesxxxxxpackage.basesvr.YourGlobalModule;
import webpiecesxxxxxpackage.json.JsonController;
import webpiecesxxxxxpackage.json.JsonRoutes;
import webpiecesxxxxxpackage.json.SaveApi;
import webpiecesxxxxxpackage.web.login.LoginRoutes;
import webpiecesxxxxxpackage.web.main.MainRoutes;
import webpiecesxxxxxpackage.web.secure.crud.CrudRoutes;
import webpiecesxxxxxpackage.web.secure.crudajax.AjaxCrudRoutes;

//This is where the list of Guice Modules go as well as the list of RouterModules which is the
//core of anything you want to plugin to your web app.  To make re-usable components, you create
//GuiceModule paired with a RouterModule and app developers can plug both in here.  In some cases,
//only a RouterModule is needed and in others only a GuiceModule is needed.
//BIG NOTE: The webserver loads this class from the appmeta.txt file which is passed in the
//start method below.  This is a hook for the Development server to work that is a necessary evil

//We name it ServerMeta so that 'EVERY' webpieces project, you can find this very important file
//telling you all the locations of all route classes and guice module classes for how the app is
//wired together(and for seeing which plugins are installed)
public class ProdServerMeta implements WebAppMeta {

	private static final Logger log = LoggerFactory.getLogger(ProdServerMeta.class);
	private WebAppConfig pluginConfig;

	@Override
	public void initialize(WebAppConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}

	//When using the Development Server, changes to this inner class will be recompiled automatically
	//when needed..  Changes to the outer class will not take effect until a restart
	//In production, we don't have a compiler on the classpath nor any funny classloaders so that
	//production is very very clean and the code for this non-dev server is very easy to step through
	//if you have a production issue
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(
				new GuiceModule(),
				new YourGlobalModule()
		);
	}

	@Override
    public List<Routes> getRouteModules() {
		return Lists.newArrayList(
				new RESTApiRoutes(SaveApi.class, JsonController.class),
				new MainRoutes(),
				//The Controller package regex is webpiecesxxxxxpackage.web.secure\..* so that we match webpiecesxxxxxpackage.web.secure.* Controllers 
				new LoginRoutes("/webpiecesxxxxxpackage/web/login/AppLoginController", "webpiecesxxxxxpackage.web.secure\\..*", "password"),
				new CrudRoutes(),
				new AjaxCrudRoutes(),
				new JsonRoutes()
				);
	}

	//NOTE: EACH Plugin takes a Config object such that we can add properties later without breaking your compile
	//This allows us to be more backwards compatible
	
	@Override
	public List<Plugin> getPlugins() {
		log.info("classloader for meta="+this.getClass().getClassLoader());
		return Lists.newArrayList(
				//if you want to remove hibernate, just remove it first from the build file and then remove
				//all the compile error code(it will remove more than half of the jar size of the web app actually due
				//to transitive dependencies)
				new HibernatePlugin(pluginConfig.getCmdLineArguments(), false),
				//ANY controllers in json package or subpackages are run through this filter independent of the url
				new JacksonPlugin(new JacksonConfig().setPackageFilterPattern("webpiecesxxxxxpackage.json.*")),
				new BackendPlugin(pluginConfig.getCmdLineArguments()),
				new PropertiesPlugin(new PropertiesConfig()),
				new InstallSslCertPlugin(new InstallSslCertConfig("acme://letsencrypt.org/staging"))
				);
	}

}