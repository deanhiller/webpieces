package webpiecesxxxxxpackage;

import com.google.inject.Binder;
import com.google.inject.Module;
import webpiecesxxxxxpackage.db.DbSettingsInMemory;
import webpiecesxxxxxpackage.services.DevConfig;

import java.util.Map;

public class OurDevConfig implements DevConfig {

	@Override
	public String[] getExtraArguments() {
		return null;
	}

	@Override
	public Map<String, String> getSimulatedEnvironmentProperties() {
		return Map.of(
				"DB_URL", "jdbc:log4jdbc:h2:mem:test",
				"DB_USER", "sa",
				"DB_PASSWORD", ""
		);
	}

		@Override
	public String getHibernateSettingsClazz() {
		return DbSettingsInMemory.class.getName();
	}

	@Override
	public int getHttpsPort() {
		return 8443;
	}

	@Override
	public int getHttpPort() {
		return 8080;
	}

	public Module getDevelopmentOverrides() {
		return new LocalhostOverrides();
	}

	private static class LocalhostOverrides implements Module {
		@Override
		public void configure(Binder binder) {
			//overrides for local development
		}
	}
}
