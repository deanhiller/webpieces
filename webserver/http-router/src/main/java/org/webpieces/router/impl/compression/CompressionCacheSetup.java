package org.webpieces.router.impl.compression;

import java.util.List;

import org.webpieces.router.impl.StaticRoute;

public interface CompressionCacheSetup {

	void setupCache(List<StaticRoute> staticRoutes);

}
