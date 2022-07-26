package webpiecesxxxxxpackage.services;

/**
 * Goes in re-usable location so all your dev servers can be modified
 * 
 * @author dean
 *
 */
public interface DevConfig {
    String[] getExtraArguments();

    String getHibernateSettingsClazz();

    int getHttpsPort();

    int getHttpPort();

}
