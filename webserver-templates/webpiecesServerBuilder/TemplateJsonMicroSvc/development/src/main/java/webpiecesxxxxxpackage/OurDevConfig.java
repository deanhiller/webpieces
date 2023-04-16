package webpiecesxxxxxpackage;

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
				"REQ_ENV_VAR", "my value"
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

}
