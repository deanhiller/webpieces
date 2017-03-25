package org.webpieces.templating.api;

import java.util.Map;

public interface RouterLookup {

	String fetchUrl(String routeId, Map<String, String> args);
	
	String pathToUrlEncodedHash(String relativeUrlPath);
}
