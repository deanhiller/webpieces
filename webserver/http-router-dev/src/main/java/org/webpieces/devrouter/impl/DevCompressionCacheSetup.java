package org.webpieces.devrouter.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.impl.StaticRoute;
import org.webpieces.router.impl.compression.CompressionCacheSetup;

public class DevCompressionCacheSetup implements CompressionCacheSetup {

	private static final Logger log = LoggerFactory.getLogger(DevCompressionCacheSetup.class);
	
	@Override
	public void setupCache(List<StaticRoute> staticRoutes) {
		log.info("SHORT CIRCUIT CompressionCacheSetup since we are running in development mode");
	}
}
