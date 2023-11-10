package org.webpieces.googleauth.api;

import com.google.inject.Module;
import org.webpieces.googleauth.impl.AuthRoutes;
import org.webpieces.googleauth.impl.GoogleAuthModule;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.util.cmdline2.Arguments;

import java.util.List;

public class GoogleAuthPlugin implements Plugin {

    public static final String USER_ID_TOKEN = "userId";
    private Arguments arguments;
    private GoogleAuthConfig authRouteIdSet;
    private Class<? extends SaveUser> saveUser;

    public GoogleAuthPlugin(Arguments arguments, GoogleAuthConfig authRouteIdSet, Class<? extends SaveUser> saveUser) {
        this.arguments = arguments;
        this.authRouteIdSet = authRouteIdSet;
        this.saveUser = saveUser;
    }
    @Override
    public List<Module> getGuiceModules() {
        return List.of(new GoogleAuthModule(arguments, authRouteIdSet, saveUser));
    }

    @Override
    public List<Routes> getRouteModules() {
        return List.of(new AuthRoutes(authRouteIdSet));
    }
}
