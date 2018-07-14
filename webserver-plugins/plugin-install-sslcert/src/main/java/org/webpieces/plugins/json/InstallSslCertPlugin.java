package org.webpieces.plugins.json;

import java.util.List;

import org.webpieces.router.api.routing.Plugin;
import org.webpieces.router.api.routing.Routes;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class InstallSslCertPlugin implements Plugin {

	
	public InstallSslCertPlugin() {
		super();
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new InstallSslCertModule());
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new InstallSslCertRoutes());
	}

}
