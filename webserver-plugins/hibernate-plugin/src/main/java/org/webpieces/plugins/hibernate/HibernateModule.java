package org.webpieces.plugins.hibernate;

import java.io.IOException;

import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class HibernateModule extends AbstractModule {

	private static final Logger log = LoggerFactory.getLogger(HibernateModule.class);
	private String persistenceUnit;

	public HibernateModule(String persistenceUnit) {
		this.persistenceUnit = persistenceUnit;
	}
	
	@Override
	protected void configure() {
	}

	@Singleton
	@Provides
	public EntityManagerFactory providesSessionFactory() throws IOException {
		log.info("Loading Hibernate");
		EntityManagerFactory factory = Persistence.createEntityManagerFactory(persistenceUnit);
		log.info("Done loading Hibernate");
		return factory;
	}

}
