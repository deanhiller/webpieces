package org.webpieces.devrouter.impl;

import java.util.List;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;
import org.webpieces.router.impl.StaticRoute;
import org.webpieces.router.impl.compression.CompressionCacheSetup;

public class DevCompressionCacheSetup implements CompressionCacheSetup {

	private static final Logger log = LoggerFactory.getLogger(DevCompressionCacheSetup.class);
	
	@Override
	public void setupCache(List<StaticRoute> staticRoutes) {
		log.info("SHORT CIRCUIT CompressionCacheSetup since we are running in development mode");
	}

	@Override
	public String relativeUrlToHash(String path) {
		return null;
	}
}
