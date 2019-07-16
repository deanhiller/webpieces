package org.webpieces.router.api.routes;

import java.util.List;

import org.webpieces.router.api.plugins.Plugin;

public interface WebAppMeta extends Plugin {

	void initialize(WebAppConfig pluginConfig);

	public List<Plugin> getPlugins();
	
}
