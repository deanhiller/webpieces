package org.webpieces.plugins.hibernate.app;

import java.util.List;

import org.webpieces.plugins.hibernate.HibernateModule;
import org.webpieces.plugins.hsqldb.H2DbModule;
import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class HibernateAppMeta implements WebAppMeta {
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(
				new H2DbModule(),
				new HibernateModule("fortest")
				);
	}
	
	public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new HibernateRouteModule());
	}
	
}