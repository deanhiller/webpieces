package org.webpieces.webserver.https.app;

import java.util.List;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;
import org.webpieces.webserver.EmptyModule;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class HttpsMeta implements WebAppMeta {
	@Override
    public List<Module> getGuiceModules() {
		return Lists.newArrayList(new EmptyModule());
	}
	
	@Override
    public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new HttpsRouteModule());
	}
	
}