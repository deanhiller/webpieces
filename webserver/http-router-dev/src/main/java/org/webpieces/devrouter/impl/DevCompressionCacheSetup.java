package org.webpieces.devrouter.impl;

import java.util.List;

import org.webpieces.router.impl.compression.CompressionCacheSetup;
import org.webpieces.router.impl.compression.FileMeta;
import org.webpieces.router.impl.routers.EStaticRouter;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public class DevCompressionCacheSetup implements CompressionCacheSetup {

	private static final Logger log = LoggerFactory.getLogger(DevCompressionCacheSetup.class);
	
	@Override
	public void setupCache(List<EStaticRouter> staticRoutes) {
		log.info("SHORT CIRCUIT CompressionCacheSetup since we are running in development mode");
	}

	@Override
	public FileMeta relativeUrlToHash(String path) {
		return null;
	}
}
