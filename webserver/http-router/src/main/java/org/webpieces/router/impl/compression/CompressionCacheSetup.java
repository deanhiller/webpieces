package org.webpieces.router.impl.compression;

import java.util.List;

import org.webpieces.router.impl.routers.FStaticRouter;

public interface CompressionCacheSetup {

	void setupCache(List<FStaticRouter> list);

	FileMeta relativeUrlToHash(String path);
	
}
