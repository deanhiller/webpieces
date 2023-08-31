package org.webpieces.auth0.api;

import com.google.inject.Binder;
import com.google.inject.Module;
import org.webpieces.auth0.impl.Auth0ApiConfig;
import org.webpieces.util.cmdline2.Arguments;

import java.util.function.Supplier;

public class Auth0Module implements Module {

    private final Supplier<String> auth0Domain;
    private final Supplier<String> auth0ClientId;
    private final Supplier<String> auth0ClientSecret;
    private Auth0Config authRouteIdSet;

    public Auth0Module(Arguments args, Auth0Config authRouteIdSet) {
        auth0Domain = args.createRequiredEnvVar("AUTH0_DOMAIN", "asdf.com", "Auth0 domain to contact", (s) -> s);
        auth0ClientId = args.createRequiredEnvVar("AUTH0_CLIENT_ID", "deansAuth0ClientId", "Auth0 client id to run auth against", (s) -> s);
        auth0ClientSecret = args.createRequiredEnvVar("AUTH0_CLIENT_SECRET", "deansAuth0Secret", "Auth0 secret to run auth against", (s) -> s);
        this.authRouteIdSet = authRouteIdSet;
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(Auth0ApiConfig.class).toInstance(
                new Auth0ApiConfig(auth0Domain.get(), auth0ClientId.get(), auth0ClientSecret.get()));

        binder.bind(Auth0Config.class).toInstance(authRouteIdSet);
    }

}
