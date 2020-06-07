package org.webpieces.plugin.sslcert;

import java.util.List;

import org.webpieces.plugin.backend.BackendPlugin;
import org.webpieces.plugin.secure.sslcert.InstallSslCertConfig;
import org.webpieces.plugin.secure.sslcert.InstallSslCertPlugin;
import org.webpieces.plugins.fortesting.FillerRoutes;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.router.api.routes.WebAppConfig;
import org.webpieces.router.api.routes.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class SslCertMeta implements WebAppMeta {
	private WebAppConfig pluginConfig;

	@Override
	public void initialize(WebAppConfig pluginConfig) {
		this.pluginConfig = pluginConfig;
	}
	
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new NoSslEmptyModule());
	}
	
	@Override
    public List<Routes> getRouteModules() {
		return Lists.newArrayList(new FillerRoutes());
	}
	
	@Override
	public List<Plugin> getPlugins() {
		return Lists.newArrayList(
				new BackendPlugin(pluginConfig.getCmdLineArguments()),
				new InstallSslCertPlugin(new InstallSslCertConfig("acme://letsencrypt.org/staging"))
		);
	}
}
