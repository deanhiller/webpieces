package webpiecesxxxxxpackage.db;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.persistence.SharedCacheMode;
import javax.persistence.ValidationMode;
import javax.persistence.spi.ClassTransformer;
import javax.persistence.spi.PersistenceUnitInfo;
import javax.persistence.spi.PersistenceUnitTransactionType;
import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import io.micrometer.core.instrument.MeterRegistry;

/**
 * When persistence.xml is found in gradles output/java/resources/META-INF, it tries to scan
 * output/java/resources for class files and entities and there are no class files.
 * Instead this allows us to scan the jar that DbSettingsProd is located in. 
 */
public class DbSettingsProd implements PersistenceUnitInfo {

	private static final Logger log = LoggerFactory.getLogger(DbSettingsProd.class);

	private Properties properties = new Properties();

	private HikariDataSource dataSource;
	
	@Inject
	public DbSettingsProd(MeterRegistry metrics) {
        //<!-- property name="javax.persistence.jdbc.driver" value="org.h2.Driver" /-->
		//properties.setProperty("javax.persistence.jdbc.driver", "net.sf.log4jdbc.DriverSpy");
		//properties.setProperty("javax.persistence.jdbc.url", "jdbc:log4jdbc:h2:mem:test");
		//properties.setProperty("javax.persistence.jdbc.user", "sa");
		//properties.setProperty("javax.persistence.jdbc.password", "");
		properties.setProperty("hibernate.dialect", "org.hibernate.dialect.H2Dialect");
		properties.setProperty("hibernate.hbm2ddl.auto", "update");
		properties.setProperty("hibernate.show_sql", "false");
		properties.setProperty("hibernate.format_sql", "false");
		properties.setProperty("hibernate.transaction.flush_before_completion", "true");
		//properties.setProperty("hibernate.connection.provider_class", "org.hibernate.hikaricp.internal.HikariCPConnectionProvider");
		
		HikariConfig config = new HikariConfig();
        config.setDriverClassName("net.sf.log4jdbc.DriverSpy");
        config.setJdbcUrl("jdbc:log4jdbc:h2:mem:test");
        config.setUsername("sa");
        config.setPassword("");
        config.setMetricRegistry(metrics);
         
        dataSource = new HikariDataSource(config);
	}
	
	@Override
	public String getPersistenceUnitName() {
		return "production";
	}

	@Override
	public String getPersistenceProviderClassName() {
		return "org.hibernate.jpa.HibernatePersistenceProvider";
	}

	@Override
	public PersistenceUnitTransactionType getTransactionType() {
		return PersistenceUnitTransactionType.RESOURCE_LOCAL;
	}

	@Override
	public DataSource getJtaDataSource() {
		return null;
	}

	@Override
	public DataSource getNonJtaDataSource() {
		return dataSource;
	}

	@Override
	public List<String> getMappingFileNames() {
		return null;
	}

	@Override
	public List<URL> getJarFileUrls() {
		return null;
	}

	/**
	 * BE VERY careful here.  Hibernate has to work if the *.class file is on filesystem vs. on jar
	 * and the code is quite different
	 */
	@Override
	public URL getPersistenceUnitRootUrl() {
		String name = DbSettingsInMemory.class.getSimpleName() + ".class";
		URL url = DbSettingsInMemory.class.getResource(name);
		String file = url.getFile();
		int length = file.length() - name.length(); 
		String root = file.substring(0, length-1);
		try {
			URL rootUrl = new URL(url.getProtocol(), url.getHost(), root);
			log.info("RootURL for scanning="+rootUrl);
			return rootUrl;
		} catch (MalformedURLException e) {
			throw new RuntimeException("Bug", e);
		}
	}

	@Override
	public List<String> getManagedClassNames() {
		return null;
	}

	@Override
	public boolean excludeUnlistedClasses() {
		return false;
	}

	@Override
	public SharedCacheMode getSharedCacheMode() {
		return SharedCacheMode.ENABLE_SELECTIVE;
	}

	@Override
	public ValidationMode getValidationMode() {
		return ValidationMode.AUTO;
	}

	@Override
	public Properties getProperties() {
		return properties;
	}

	@Override
	public String getPersistenceXMLSchemaVersion() {
		return null;
	}

	@Override
	public ClassLoader getClassLoader() {
		throw new UnsupportedOperationException("Not supported.  webpieces needs to pass in Development Classloader");
	}

	@Override
	public void addTransformer(ClassTransformer transformer) {
	}

	@Override
	public ClassLoader getNewTempClassLoader() {
		return null;
	}
	
	
	
}
