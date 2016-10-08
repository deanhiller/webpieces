package org.webpieces.plugins.hibernate;

import java.io.Closeable;
import java.io.IOException;

import javax.inject.Singleton;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class HibernateModule extends AbstractModule {

	private String configFile;

	public HibernateModule(String configFile) {
		this.configFile = configFile;
	}
	
	@Override
	protected void configure() {
	}
	
	@Singleton
	@Provides
	public SessionFactory providesSessionFactory() throws IOException {
		// A SessionFactory is set up once for an application!
		final StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
				.configure(configFile) // configures settings from hibernate.cfg.xml
				.build();
		
		try(FactoryCreator creator = new FactoryCreator(registry)) {
			SessionFactory factory = creator.create();
			return factory;
		}
	}

	private class FactoryCreator implements Closeable {

		private StandardServiceRegistry registry;

		public FactoryCreator(StandardServiceRegistry registry) {
			this.registry = registry;
		}

		public SessionFactory create() {
			return new MetadataSources( registry ).buildMetadata().buildSessionFactory();
		}
		
		@Override
		public void close() throws IOException {
			StandardServiceRegistryBuilder.destroy( registry );
		}
	}
}
