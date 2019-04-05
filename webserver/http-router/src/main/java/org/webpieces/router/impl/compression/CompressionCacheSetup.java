package org.webpieces.router.impl.compression;

import java.util.List;

import org.webpieces.router.impl.routers.EStaticRouter;

public interface CompressionCacheSetup {

	void setupCache(List<EStaticRouter> list);

	FileMeta relativeUrlToHash(String path);
	
}
