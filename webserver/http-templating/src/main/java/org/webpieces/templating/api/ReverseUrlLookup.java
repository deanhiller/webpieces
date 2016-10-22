package org.webpieces.templating.api;

import java.util.Map;

public interface ReverseUrlLookup {

	String fetchUrl(String routeId, Map<String, String> args);
}
