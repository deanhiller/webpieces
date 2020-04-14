package org.webpieces.plugins.hibernate;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;

import io.micrometer.core.instrument.MeterRegistry;

import org.hibernate.cfg.AvailableSettings;
import org.hibernate.jpa.HibernatePersistenceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.router.api.extensions.EntityLookup;

import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceUnitInfo;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class HibernateModule extends AbstractModule {

	private static final Logger log = LoggerFactory.getLogger(HibernateModule.class);
	private Supplier<String> persistenceUnit;
	private ClassLoader entityClassLoader;
	private Supplier<Boolean> loadByClassFile;

	public HibernateModule(Supplier<String> persistenceUnit2, Supplier<Boolean> loadByClassFile) {
		this.loadByClassFile = loadByClassFile;
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
	public EntityManagerFactory providesSessionFactory(MeterRegistry metrics) throws IOException {
		boolean loadByClassMeta = loadByClassFile.get();
		String pu = persistenceUnit.get();		
		if(loadByClassMeta) {
			return loadByClassMeta(pu, metrics);
		} else {
			return createEntityMgrFromPuFile(pu);
		}
	}

	private EntityManagerFactory createEntityMgrFromPuFile(String pu) {
		log.info("Loading Hibernate from xml file.  ENTITY classloader="+entityClassLoader+" hibernate classloader="+this.getClass().getClassLoader()+" pu="+pu);

		Map<String, Object> properties = createClassLoaderProperty();		
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(pu, properties );
		log.info("Done loading Hibernate");
		return factory;
	}

	private EntityManagerFactory loadByClassMeta(String clazz, MeterRegistry metrics) {
		log.info("Loading Hibernate from class meta.  ENTITY classloader="+entityClassLoader+" hibernate classloader="+this.getClass().getClassLoader()+" class="+clazz);

		Class<?> loadClass = null;
		try {
			loadClass = entityClassLoader.loadClass(clazz);
			Constructor<?> ctr = loadClass.getDeclaredConstructor(MeterRegistry.class);
			Object newInstance = ctr.newInstance(metrics);
			if(!(newInstance instanceof PersistenceUnitInfo))
				throw new IllegalArgumentException(clazz+" is not an instanceof PersistenceUnitInfo and must be");
		
			PersistenceUnitInfo info = (PersistenceUnitInfo) newInstance;
			//Can probably remove this proxy because overrideProperties should override BUT this needs lots of testing
			//on DevelopmentServer to make sure changes to code in many places don't break especially changing Dbo's
			PersistenceUnitInfo proxy = new PersistenceUnitInfoProxy(info, entityClassLoader);
			
			Map<String, Object> overrideProperties = createClassLoaderProperty();		
			
			return new HibernatePersistenceProvider().createContainerEntityManagerFactory(proxy, overrideProperties);
		} catch(NoSuchMethodException e) {
			throw new IllegalStateException("A constructor "+loadClass.getSimpleName()+"(MeterRegistry metrics) was not found in class="+loadClass.getName());
		} catch(ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | SecurityException e) {
			throw new IllegalStateException("Could not construct DB settings", e);
		}
		
	}

	private Map<String, Object> createClassLoaderProperty() {
		Collection<ClassLoader> classLoaders = new ArrayList<>();
		classLoaders.add(entityClassLoader);
		Map<String, Object> properties = new HashMap<>();
		properties.put(AvailableSettings.CLASSLOADERS, classLoaders);
		return properties;
	}
	
}
