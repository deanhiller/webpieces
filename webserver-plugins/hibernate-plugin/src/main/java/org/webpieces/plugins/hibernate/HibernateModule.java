package org.webpieces.plugins.hibernate;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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
		//requireBinding(DatabaseStarted.class);
		
		try {
			kickStart();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private void kickStart() throws ClassNotFoundException, SQLException {
		Class.forName("org.h2.Driver");
		Class.forName("net.sf.log4jdbc.DriverSpy");
		Connection conn = DriverManager.getConnection("jdbc:log4jdbc:h2:mem:unitTestDb");
		conn.close();
	}

	@Singleton
	@Provides
	public EntityManagerFactory providesSessionFactory() throws IOException {
		return Persistence.createEntityManagerFactory(persistenceUnit);
	}

}
