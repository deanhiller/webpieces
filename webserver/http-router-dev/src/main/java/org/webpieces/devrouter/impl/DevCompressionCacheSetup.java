package org.webpieces.devrouter.impl;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.impl.compression.CompressionCacheSetup;
import org.webpieces.router.impl.compression.FileMeta;
import org.webpieces.router.impl.routers.FStaticRouter;

public class DevCompressionCacheSetup implements CompressionCacheSetup {

	private static final Logger log = LoggerFactory.getLogger(DevCompressionCacheSetup.class);
	
	@Override
	public void setupCache(List<FStaticRouter> staticRoutes) {
		log.info("SHORT CIRCUIT CompressionCacheSetup since we are running in development mode");
	}

	@Override
	public FileMeta relativeUrlToHash(String path) {
		return null;
	}
}
