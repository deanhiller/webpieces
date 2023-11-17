package org.webpieces.auth0.impl;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import org.webpieces.auth0.api.Auth0Config;
import org.webpieces.auth0.api.SaveUser;
import org.webpieces.auth0.client.api.Auth0Api;
import org.webpieces.auth0.mgmt.api.AuthManagementApi;
import org.webpieces.microsvc.client.api.RESTClientCreator;
import org.webpieces.util.HostWithPort;
import org.webpieces.util.cmdline2.Arguments;
import org.webpieces.util.context.PlatformHeaders;

import javax.inject.Singleton;
import java.util.function.Supplier;

public class Auth0Module implements Module {

    private final Supplier<String> auth0Domain;
    private final Supplier<String> auth0ClientId;
    private final Supplier<String> auth0ClientSecret;
    private final Supplier<String> auth0Audience;
    private Auth0Config authRouteIdSet;
    private Class<? extends SaveUser> saveUser;

    public Auth0Module(Arguments args, Auth0Config authRouteIdSet, Class<? extends SaveUser> saveUser) {
        auth0Domain = args.createRequiredEnvVar("AUTH0_DOMAIN", "asdf.com", "Auth0 domain to contact", (s) -> s);
        auth0ClientId = args.createRequiredEnvVar("AUTH0_CLIENT_ID", "deansAuth0ClientId", "Auth0 client id to run auth against", (s) -> s);
        auth0ClientSecret = args.createRequiredEnvVar("AUTH0_CLIENT_SECRET", "deansAuth0Secret", "Auth0 secret to run auth against", (s) -> s);
        auth0Audience = args.createOptionalEnvVar("AUTH0_AUDIENCE", null, "The id of the api in Auth0 dashboard", (s) -> s);

        this.authRouteIdSet = authRouteIdSet;
        this.saveUser = saveUser;
        if(saveUser == null)
            throw new IllegalArgumentException("saveUser param cannot be null and was");
    }

    @Override
    public void configure(Binder binder) {

        Multibinder<PlatformHeaders> headerBinder = Multibinder.newSetBinder(binder, PlatformHeaders.class);
        headerBinder.addBinding().toInstance(Auth0Header.AUTH_TOKEN);

        binder.bind(Auth0ApiConfig.class).toInstance(
                new Auth0ApiConfig(auth0Domain.get(), auth0ClientId.get(), auth0ClientSecret.get(), auth0Audience.get()));

        binder.bind(Auth0Config.class).toInstance(authRouteIdSet);

        binder.bind(SaveUser.class).to(saveUser).asEagerSingleton();
    }

    @Singleton
    @Provides
    public Auth0Api createApi(RESTClientCreator clientCreator, Auth0ApiConfig config) {
        return clientCreator.createClient(Auth0Api.class, new HostWithPort(config.getAuth0Domain(), 443));
    }

    @Singleton
    @Provides
    public AuthManagementApi createMgmtApi(RESTClientCreator clientCreator, Auth0ApiConfig config) {
        return clientCreator.createClient(AuthManagementApi.class, new HostWithPort(config.getAuth0Domain(), 443));
    }
}
