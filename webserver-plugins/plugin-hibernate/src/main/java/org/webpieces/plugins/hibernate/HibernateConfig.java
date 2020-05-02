package org.webpieces.plugins.hibernate;

@Deprecated
public class HibernateConfig {

	private String persistenceUnit;
	
	public HibernateConfig(String persistenceUnit) {
		super();
		this.persistenceUnit = persistenceUnit;
	}

	public String getPersistenceUnit() {
		return persistenceUnit;
	}

}
