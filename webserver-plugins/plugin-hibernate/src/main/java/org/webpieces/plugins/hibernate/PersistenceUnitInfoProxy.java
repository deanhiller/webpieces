package org.webpieces.plugins.hibernate;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

public class PersistenceUnitInfoProxy implements PersistenceUnitInfo {

	private PersistenceUnitInfo info;
	private ClassLoader classLoader;

	public PersistenceUnitInfoProxy(PersistenceUnitInfo info, ClassLoader cl) {
		this.info = info;
		this.classLoader = cl;
	}

	public String getPersistenceUnitName() {
		return info.getPersistenceUnitName();
	}

	public String getPersistenceProviderClassName() {
		return info.getPersistenceProviderClassName();
	}

	public PersistenceUnitTransactionType getTransactionType() {
		return info.getTransactionType();
	}

	public DataSource getJtaDataSource() {
		return info.getJtaDataSource();
	}

	public DataSource getNonJtaDataSource() {
		return info.getNonJtaDataSource();
	}

	public List<String> getMappingFileNames() {
		return info.getMappingFileNames();
	}

	public List<URL> getJarFileUrls() {
		return info.getJarFileUrls();
	}

	public URL getPersistenceUnitRootUrl() {
		return info.getPersistenceUnitRootUrl();
	}

	public List<String> getManagedClassNames() {
		return info.getManagedClassNames();
	}

	public boolean excludeUnlistedClasses() {
		return info.excludeUnlistedClasses();
	}

	public SharedCacheMode getSharedCacheMode() {
		return info.getSharedCacheMode();
	}

	public ValidationMode getValidationMode() {
		return info.getValidationMode();
	}

	public Properties getProperties() {
		return info.getProperties();
	}

	public String getPersistenceXMLSchemaVersion() {
		return info.getPersistenceXMLSchemaVersion();
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public ClassLoader getNewTempClassLoader() {
		return info.getNewTempClassLoader();
	}

	@Override
	public void addTransformer(ClassTransformer transformer) {
		throw new UnsupportedOperationException("not supported");
	}


}
