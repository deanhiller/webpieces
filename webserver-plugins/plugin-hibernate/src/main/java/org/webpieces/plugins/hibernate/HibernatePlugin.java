package org.webpieces.plugins.hibernate;

import java.util.List;
import java.util.function.Supplier;

import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class HibernatePlugin implements Plugin {

	private static final Logger log = LoggerFactory.getLogger(HibernatePlugin.class);
	
	public static final String PERSISTENCE_UNIT_KEY = "hibernate.persistenceunit";
	public static final String PERSISTENCE_TEST_UNIT = "hibernatefortest";

	private Supplier<String> persistenceUnit;

	public HibernatePlugin(HibernateConfig config) {
		persistenceUnit = () -> config.getPersistenceUnit();
	}
	
	public HibernatePlugin(Arguments cmdLineArgs) {
		log.info("classloader="+getClass().getClassLoader());
		this.persistenceUnit = cmdLineArgs.createRequiredArg(PERSISTENCE_UNIT_KEY, "The named persistence unit from the list of them inside META-INF/persistence.xml", (s) -> s);
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
