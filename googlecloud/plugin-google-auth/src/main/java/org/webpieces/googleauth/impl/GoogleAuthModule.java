package org.webpieces.googleauth.impl;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Provides;
import org.webpieces.googleauth.api.SaveUser;
import org.webpieces.googleauth.client.api.AuthApi;
import org.webpieces.googleauth.api.GoogleAuthConfig;
import org.webpieces.microsvc.client.api.RESTClientCreator;
import org.webpieces.util.HostWithPort;
import org.webpieces.util.cmdline2.Arguments;

import javax.inject.Singleton;
import java.util.function.Supplier;

public class GoogleAuthModule implements Module {

    private final Supplier<String> callbackUrl;
    private final Supplier<String> authClientId;
    private final Supplier<String> authClientSecret;
    private GoogleAuthConfig authRouteIdSet;
    private Class<? extends SaveUser> saveUser;

    public GoogleAuthModule(Arguments args, GoogleAuthConfig authRouteIdSet, Class<? extends SaveUser> saveUser) {
        callbackUrl = args.createRequiredEnvVar("GOOGLE_CALLBACK_URL", "set in OurDevConfig.java and add DNS xxxxxx.com pointing to 127.0.0.1 for local dev", "Google callback url.  Add to OurDevConfig.java xxxx.com/callback AND add DNS xxxx.com -> 127.0.0.1 for local dvelopment");
        authClientId = args.createRequiredEnvVar("GOOGLE_CLIENT_ID", "deansAuth0ClientId", "Auth0 client id to run auth against", (s) -> s);
        authClientSecret = args.createRequiredEnvVar("GOOGLE_CLIENT_SECRET", "deansAuth0Secret", "Auth0 secret to run auth against", (s) -> s);

        this.authRouteIdSet = authRouteIdSet;
        this.saveUser = saveUser;
        if(saveUser == null)
            throw new IllegalArgumentException("saveUser param cannot be null and was");
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(AuthApiConfig.class).toInstance(
                new AuthApiConfig(callbackUrl.get(), authClientId.get(), authClientSecret.get()));

        binder.bind(GoogleAuthConfig.class).toInstance(authRouteIdSet);

        binder.bind(SaveUser.class).to(saveUser).asEagerSingleton();
    }

    @Singleton
    @Provides
    public AuthApi createApi(RESTClientCreator clientCreator, AuthApiConfig config) {
        return clientCreator.createClient(AuthApi.class, new HostWithPort("oauth2.googleapis.com", 443));
    }

}
