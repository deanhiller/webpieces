package org.webpieces.webserver.impl;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import javax.inject.Inject;

import org.webpieces.router.api.RouterService;
import org.webpieces.router.impl.compression.FileMeta;
import org.webpieces.templating.api.RouterLookup;
import org.webpieces.util.net.URLEncoder;

public class RouterLookupProxy implements RouterLookup {

	private RouterService router;

	@Inject
	public RouterLookupProxy(RouterService router) {
		this.router = router;
	}
	
	@Override
	public String fetchUrl(String routeId, Map<String, String> args) {
		return router.convertToUrl(routeId, args, false );
	}

	@Override
	public String pathToUrlEncodedHash(String relativeUrlPath) {
		FileMeta hashMeta = router.relativeUrlToHash(relativeUrlPath);
		if(hashMeta == null || hashMeta.getHash() == null)
			return null;
		
		return URLEncoder.encode(hashMeta.getHash(), StandardCharsets.UTF_8);
	}

}
