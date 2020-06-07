package org.webpieces.plugin.hibernate;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

public class PersistenceUnitInfoImpl implements PersistenceUnitInfo {

	private String persistenceUnitName;

	private String persistenceProviderClassName;

	private PersistenceUnitTransactionType transactionType;

	private DataSource nonJtaDataSource;

	private DataSource jtaDataSource;

	private List<String> mappingFileNames = new LinkedList<>();

	private List<URL> jarFileUrls = new LinkedList<>();

	private URL persistenceUnitRootUrl;

	private List<String> managedClassNames = new LinkedList<>();

	private boolean excludeUnlistedClasses = false;

	private Properties properties = new Properties();

	private String persistenceProviderPackageName;

	private SharedCacheMode cacheMode;

	private ValidationMode validationMode;

	private ClassLoader classLoader;

	public void setPersistenceUnitName(String persistenceUnitName) {
		this.persistenceUnitName = persistenceUnitName;
	}

	public String getPersistenceUnitName() {
		return this.persistenceUnitName;
	}

	public void setPersistenceProviderClassName(String persistenceProviderClassName) {
		this.persistenceProviderClassName = persistenceProviderClassName;
	}

	public String getPersistenceProviderClassName() {
		return this.persistenceProviderClassName;
	}

	public void setTransactionType(PersistenceUnitTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	public PersistenceUnitTransactionType getTransactionType() {
		if (this.transactionType != null) {
			return this.transactionType;
		}
		else {
			return (this.jtaDataSource != null ?
					PersistenceUnitTransactionType.JTA : PersistenceUnitTransactionType.RESOURCE_LOCAL);
		}
	}

	public void setJtaDataSource(DataSource jtaDataSource) {
		this.jtaDataSource = jtaDataSource;
	}

	public DataSource getJtaDataSource() {
		return this.jtaDataSource;
	}

	public void setNonJtaDataSource(DataSource nonJtaDataSource) {
		this.nonJtaDataSource = nonJtaDataSource;
	}

	public DataSource getNonJtaDataSource() {
		return this.nonJtaDataSource;
	}

	public void addMappingFileName(String mappingFileName) {
		this.mappingFileNames.add(mappingFileName);
	}

	public List<String> getMappingFileNames() {
		return this.mappingFileNames;
	}

	public void addJarFileUrl(URL jarFileUrl) {
		this.jarFileUrls.add(jarFileUrl);
	}

	public List<URL> getJarFileUrls() {
		return this.jarFileUrls;
	}

	public void setPersistenceUnitRootUrl(URL persistenceUnitRootUrl) {
		this.persistenceUnitRootUrl = persistenceUnitRootUrl;
	}

	public URL getPersistenceUnitRootUrl() {
		return this.persistenceUnitRootUrl;
	}

	public void addManagedClassName(String managedClassName) {
		this.managedClassNames.add(managedClassName);
	}

	public List<String> getManagedClassNames() {
		return this.managedClassNames;
	}

	public void setExcludeUnlistedClasses(boolean excludeUnlistedClasses) {
		this.excludeUnlistedClasses = excludeUnlistedClasses;
	}

	public boolean excludeUnlistedClasses() {
		return this.excludeUnlistedClasses;
	}

	public void addProperty(String name, String value) {
		if (this.properties == null) {
			this.properties = new Properties();
		}
		this.properties.setProperty(name, value);
	}

	public void setProperties(Properties properties) {
		this.properties = properties;
	}

	public Properties getProperties() {
		return this.properties;
	}

	public void setPersistenceProviderPackageName(String persistenceProviderPackageName) {
		this.persistenceProviderPackageName = persistenceProviderPackageName;
	}

	public String getPersistenceProviderPackageName() {
		return this.persistenceProviderPackageName;
	}

	/**
	 * This implementation returns the default ClassLoader.
	 * @see org.springframework.util.ClassUtils#getDefaultClassLoader()
	 */
	public ClassLoader getClassLoader() {
		return classLoader;
	}
	
	public void setClassLoader(ClassLoader cl) {
		this.classLoader = cl;
	}

	/**
	 * This implementation throws an UnsupportedOperationException.
	 */
	public void addTransformer(ClassTransformer classTransformer) {
		throw new UnsupportedOperationException("addTransformer not supported");
	}

	/**
	 * This implementation throws an UnsupportedOperationException.
	 */
	public ClassLoader getNewTempClassLoader() {
		throw new UnsupportedOperationException("getNewTempClassLoader not supported");
	}


	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("PersistenceUnitInfo: name '");
		builder.append(this.persistenceUnitName);
		builder.append("', root URL [");
		builder.append(this.persistenceUnitRootUrl);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		return cacheMode;
	}

	@Override
	public ValidationMode getValidationMode() {
		return validationMode;
	}

	@Override
	public String getPersistenceXMLSchemaVersion() {
		return null;
	}

}
