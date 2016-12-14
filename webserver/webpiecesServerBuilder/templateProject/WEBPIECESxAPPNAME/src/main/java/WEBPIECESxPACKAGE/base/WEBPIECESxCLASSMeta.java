package WEBPIECESxPACKAGE.base;

import java.util.List;
import java.util.Map;

import org.webpieces.plugins.hibernate.HibernatePlugin;
import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

//This is where the list of Guice Modules go as well as the list of RouterModules which is the
//core of anything you want to plugin to your web app.  To make re-usable components, you create
//GuiceModule paired with a RouterModule and app developers can plug both in here.  In some cases,
//only a RouterModule is needed and in others only a GuiceModule is needed.
//BIG NOTE: The webserver loads this class from the appmeta.txt file which is passed in the
//start method below.  This is a hook for the Development server to work that is a necessary evil
public class WEBPIECESxCLASSMeta implements WebAppMeta {


	private String persistenceUnit;

	@Override
	public void initialize(Map<String, String> props) {
		persistenceUnit = props.get(HibernatePlugin.PERSISTENCE_UNIT_KEY);
	}
	
	//When using the Development Server, changes to this inner class will be recompiled automatically
	//when needed..  Changes to the outer class will not take effect until a restart
	//In production, we don't have a compiler on the classpath nor any funny classloaders so that
	//production is very very clean and the code for this non-dev server is very easy to step through
	//if you have a production issue
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new WEBPIECESxCLASSGuiceModule());
	}
	
	@Override
    public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new WEBPIECESxCLASSRouteModule());
	}

	@Override
	public List<Plugin> getPlugins() {
		return Lists.newArrayList(
				//if you want to remove hibernate, just remove it first from the build file and then remove
				//all the compile error code
				new HibernatePlugin(persistenceUnit)
				);
	}

}