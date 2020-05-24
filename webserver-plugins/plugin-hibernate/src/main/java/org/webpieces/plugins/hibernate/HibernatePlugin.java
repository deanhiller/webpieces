package org.webpieces.plugins.hibernate;

import java.util.List;
import java.util.function.Supplier;

import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.util.cmdline2.Arguments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.inject.Module;

public class HibernatePlugin implements Plugin {

	private static final Logger log = LoggerFactory.getLogger(HibernatePlugin.class);
	
	public static final String PERSISTENCE_UNIT_KEY = "hibernate.persistenceunit";
	public static final String LOAD_CLASSMETA_KEY = "hibernate.loadclassmeta";

	public static final String PERSISTENCE_TEST_UNIT = "hibernatefortest";

	private Supplier<String> persistenceUnit;
	private Supplier<Boolean> loadByClassFile;

	private HibernateConfiguration config;

	@Deprecated
	public HibernatePlugin(HibernateConfig config) {
		persistenceUnit = () -> config.getPersistenceUnit();
		loadByClassFile = () -> false; //default to false since this is legacy using file
	}
	
	public HibernatePlugin(Arguments cmdLineArgs) {
		log.info("classloader="+getClass().getClassLoader());
		this.persistenceUnit = cmdLineArgs.createRequiredArg(PERSISTENCE_UNIT_KEY, "The named persistence unit from the list of them inside META-INF/persistence.xml", (s) -> s);
		this.loadByClassFile = cmdLineArgs.createOptionalArg(LOAD_CLASSMETA_KEY, "true", "If you supply a *.class for 'hibernate.persistenceunit', set this flat to true", (s) -> convertBool(s));
	}
	
	public HibernatePlugin(HibernateConfiguration config, Arguments cmdLineArgs) {
		this.config = config;
		log.info("classloader="+getClass().getClassLoader());
		this.persistenceUnit = cmdLineArgs.createRequiredArg(PERSISTENCE_UNIT_KEY, "The named persistence unit from the list of them inside META-INF/persistence.xml", (s) -> s);
		this.loadByClassFile = cmdLineArgs.createOptionalArg(LOAD_CLASSMETA_KEY, "true", "If you supply a *.class for 'hibernate.persistenceunit', set this flat to true", (s) -> convertBool(s));
	}
	
	public static Boolean convertBool(String s) {
		return Boolean.valueOf(s);
	}
	
	@Override
	public List<Module> getGuiceModules() {
		return Lists.newArrayList(new HibernateModule(persistenceUnit, loadByClassFile));
	}

	@Override
	public List<Routes> getRouteModules() {
		return Lists.newArrayList(new HibernateRoutes(config));
	}

}
