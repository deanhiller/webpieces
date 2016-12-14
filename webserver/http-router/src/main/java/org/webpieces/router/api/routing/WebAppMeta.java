package org.webpieces.router.api.routing;

import java.util.List;
import java.util.Map;

public interface WebAppMeta extends Plugin {

	void initialize(Map<String, String> props);

	public List<Plugin> getPlugins();
	
}
