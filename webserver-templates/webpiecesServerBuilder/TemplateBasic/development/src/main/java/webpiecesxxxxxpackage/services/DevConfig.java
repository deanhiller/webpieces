package webpiecesxxxxxpackage.services;

import java.util.HashMap;
import java.util.Map;

/**
 * Goes in re-usable location so all your dev servers can be modified
 * 
 * @author dean
 *
 */
public interface DevConfig {
    String[] getExtraArguments();

    Map<String, String> getSimulatedEnvironmentProperties();

    String getHibernateSettingsClazz();

    int getHttpsPort();

    int getHttpPort();

}
