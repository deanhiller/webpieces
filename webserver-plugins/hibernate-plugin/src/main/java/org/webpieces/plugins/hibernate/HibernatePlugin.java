package org.webpieces.plugins.hibernate;

import java.util.List;

import org.webpieces.router.api.routing.RouteModule;
import org.webpieces.router.api.routing.WebAppMeta;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class HibernatePlugin implements WebAppMeta {

	private String persistenceUnit;

	public HibernatePlugin(String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new HibernateModule(persistenceUnit));
	}

	@Override
	public List<RouteModule> getRouteModules() {
		return Lists.newArrayList(new HibernateRouteModule());
	}

}
