package org.webpieces.auth0.api;

import com.google.inject.Module;
import org.webpieces.auth0.impl.Auth0Module;
import org.webpieces.auth0.impl.Auth0Routes;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.util.cmdline2.Arguments;

import java.util.List;

public class Auth0Plugin implements Plugin {

    public static final String USER_ID_TOKEN = "userId";
    private Arguments arguments;
    private Auth0Config authRouteIdSet;
    private Class<? extends SaveUser> saveUser;

    public Auth0Plugin(Arguments arguments, Auth0Config authRouteIdSet, Class<? extends SaveUser> saveUser) {
        this.arguments = arguments;
        this.authRouteIdSet = authRouteIdSet;
        this.saveUser = saveUser;
    }
    @Override
    public List<Module> getGuiceModules() {
        return List.of(new Auth0Module(arguments, authRouteIdSet, saveUser));
    }

    @Override
    public List<Routes> getRouteModules() {
        return List.of(new Auth0Routes(authRouteIdSet));
    }
}
