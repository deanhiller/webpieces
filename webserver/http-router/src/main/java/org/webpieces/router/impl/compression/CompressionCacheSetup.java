package org.webpieces.router.impl.compression;

import java.util.List;

import org.webpieces.router.impl.routers.FStaticRouter;

import com.google.inject.ImplementedBy;

@ImplementedBy(ProdCompressionCacheSetup.class)
public interface CompressionCacheSetup {

	void setupCache(List<FStaticRouter> list);

	FileMeta relativeUrlToHash(String path);
	
}
