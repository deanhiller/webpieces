package org.webpieces.plugins.hibernate;

import java.io.IOException;

import javax.inject.Singleton;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class HibernateModule extends AbstractModule {

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
		return Persistence.createEntityManagerFactory(persistenceUnit);
	}

}
