package org.webpieces.router.impl;

import org.webpieces.router.api.dto.Request;
import org.webpieces.router.impl.loader.Loader;
import org.webpieces.util.file.VirtualFile;

import com.google.inject.Module;

public class ProdRouterConfig extends RouterConfig {

	public ProdRouterConfig(VirtualFile modules, Module overrides, Loader loader) {
		super(modules, overrides, loader);
	}

	@Override
	public boolean reloadIfTextFileChanged() {
		return false;
	}

	@Override
	public void processHttpRequests(Request req) {
		RouteMeta meta = fetchRoute(req);
		
		invokeRoute(meta, req);
	}

}
