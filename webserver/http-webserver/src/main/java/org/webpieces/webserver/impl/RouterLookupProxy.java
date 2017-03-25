package org.webpieces.webserver.impl;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.router.api.RoutingService;
import org.webpieces.templating.api.RouterLookup;

public class RouterLookupProxy implements RouterLookup {

	private RoutingService router;

	@Inject
	public RouterLookupProxy(RoutingService router) {
		this.router = router;
	}
	
	@Override
	public String fetchUrl(String routeId, Map<String, String> args) {
		return router.convertToUrl(routeId, args);
	}

	@Override
	public String pathToUrlEncodedHash(String relativeUrlPath) {
		String hash = router.relativeUrlToHash(relativeUrlPath);
		if(hash == null)
			return null;
		
		try {
			return URLEncoder.encode(hash, StandardCharsets.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

}
