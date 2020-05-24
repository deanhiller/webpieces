package webpiecesxxxxxpackage;

import webpiecesxxxxxpackage.db.DbSettingsInMemory;
import webpiecesxxxxxpackage.services.DevConfig;

public class OurDevConfig implements DevConfig {

	@Override
	public String[] getExtraArguments() {
		return null;
	}

	@Override
	public String getHibernateSettingsClazz() {
		return DbSettingsInMemory.class.getName();
	}

	@Override
	public int getHttpsPort() {
		return 8080;
	}

	@Override
	public int getHttpPort() {
		return 8443;
	}

}
