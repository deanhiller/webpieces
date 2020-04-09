package webpiecesxxxxxpackage.meta;

import java.util.List;

import org.webpieces.plugins.documentation.DocumentationConfig;
import org.webpieces.plugins.documentation.WebpiecesDocumentationPlugin;
import org.webpieces.plugins.hsqldb.H2DbConfig;
import org.webpieces.plugins.hsqldb.H2DbPlugin;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

import webpiecesxxxxxpackage.base.ProdServerMeta;

public class DevServerMeta implements WebAppMeta {

	private ProdServerMeta prodMeta = new ProdServerMeta();
	
	@Override
	public void initialize(WebAppConfig pluginConfig) {
		prodMeta.initialize(pluginConfig);
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return prodMeta.getGuiceModules();
	}

	@Override
	public List<Routes> getRouteModules() {
		return prodMeta.getRouteModules();
	}

	@Override
	public List<Plugin> getPlugins() {
		List<Plugin> prodPlugins = prodMeta.getPlugins();
		List<Plugin> devPlugins = Lists.newArrayList(
				//This is only for the development server to expose a SQL GUI to use http://localhost:9000/@db
				//so the in-memory H2 DB can be queried to debug issues with your application code
				new H2DbPlugin(new H2DbConfig()),
				new WebpiecesDocumentationPlugin(new DocumentationConfig())
				);

		prodPlugins.addAll(devPlugins);
		
		return prodPlugins;
	}

}
