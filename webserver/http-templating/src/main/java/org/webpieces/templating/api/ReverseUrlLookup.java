package org.webpieces.templating.api;

import java.util.Map;

public interface ReverseUrlLookup {

	public String fetchUrl(String routeId, Map<String, String> args);
}
