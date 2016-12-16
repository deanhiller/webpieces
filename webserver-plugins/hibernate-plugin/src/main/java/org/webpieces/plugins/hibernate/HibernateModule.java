package org.webpieces.plugins.hibernate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.cfg.AvailableSettings;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class HibernateModule extends AbstractModule {

	private static final Logger log = LoggerFactory.getLogger(HibernateModule.class);
	private String persistenceUnit;
	private ClassLoader entityClassLoader;

	public HibernateModule(String persistenceUnit) {
		//get classloader so if we are in development mode, we will use that class loader for entities
		entityClassLoader = Thread.currentThread().getContextClassLoader();
		this.persistenceUnit = persistenceUnit;
	}
	
	@Override
	protected void configure() {
	}

	@Singleton
	@Provides
	public EntityManagerFactory providesSessionFactory() throws IOException {
		log.info("Loading Hibernate.  ENTITY classloader="+entityClassLoader+" hibernate classloader="+this.getClass().getClassLoader());
		Map<String, Object> properties = createClassLoaderProperty();
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(persistenceUnit, properties );
		log.info("Done loading Hibernate");
		return factory;
	}

	private Map<String, Object> createClassLoaderProperty() {
		Collection<ClassLoader> classLoaders = new ArrayList<>();
		classLoaders.add(entityClassLoader);
		Map<String, Object> properties = new HashMap<>();
		properties.put(AvailableSettings.CLASSLOADERS, classLoaders);
		return properties;
	}

}
