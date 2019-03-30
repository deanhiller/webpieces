package org.webpieces.router.api.routes;

import java.util.List;
import java.util.Map;

import org.webpieces.router.api.plugins.Plugin;

public interface WebAppMeta extends Plugin {

	void initialize(Map<String, String> props);

	public List<Plugin> getPlugins();
	
}
