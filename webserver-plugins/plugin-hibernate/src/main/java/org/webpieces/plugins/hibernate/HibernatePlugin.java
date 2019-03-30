package org.webpieces.plugins.hibernate;

import java.util.List;

import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class HibernatePlugin implements Plugin {

	private static final Logger log = LoggerFactory.getLogger(HibernatePlugin.class);
	
	public static final String PERSISTENCE_UNIT_KEY = "hibernate.persistenceunit.key";
	public static final String PERSISTENCE_TEST_UNIT = "hibernatefortest";
			
	private String persistenceUnit;

	public HibernatePlugin(HibernateConfig config) {
		log.info("classloader="+getClass().getClassLoader());
		this.persistenceUnit = config.getPersistenceUnit();
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new HibernateModule(persistenceUnit));
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new HibernateRoutes());
	}

}
