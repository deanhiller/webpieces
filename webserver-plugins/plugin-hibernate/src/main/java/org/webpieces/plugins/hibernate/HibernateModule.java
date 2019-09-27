package org.webpieces.plugins.hibernate;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.hibernate.cfg.AvailableSettings;
import org.webpieces.router.api.extensions.EntityLookup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

public class HibernateModule extends AbstractModule {

	private static final Logger log = LoggerFactory.getLogger(HibernateModule.class);
	private Supplier<String> persistenceUnit;
	private ClassLoader entityClassLoader;

	public HibernateModule(Supplier<String> persistenceUnit2) {
		//get classloader so if we are in development mode, we will use that class loader for entities
		entityClassLoader = Thread.currentThread().getContextClassLoader();
		this.persistenceUnit = persistenceUnit2;
	}
	
	@Override
	protected void configure() {
		Multibinder<EntityLookup> uriBinder = Multibinder.newSetBinder(binder(), EntityLookup.class);
	    uriBinder.addBinding().to(HibernateLookup.class);
	}

	@Singleton
	@Provides
	public EntityManagerFactory providesSessionFactory() throws IOException {
		String pu = persistenceUnit.get();
		log.info("Loading Hibernate.  ENTITY classloader="+entityClassLoader+" hibernate classloader="+this.getClass().getClassLoader()+" pu="+pu);
		Map<String, Object> properties = createClassLoaderProperty();
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(pu, properties );
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
