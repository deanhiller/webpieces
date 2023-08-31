package org.webpieces.auth0.api;

import com.google.inject.Module;
import org.webpieces.router.api.plugins.Plugin;
import org.webpieces.router.api.routes.Routes;
import org.webpieces.util.cmdline2.Arguments;

import java.util.List;

public class Auth0Plugin implements Plugin {

    private Arguments arguments;
    private Auth0Config authRouteIdSet;

    public Auth0Plugin(Arguments arguments, Auth0Config authRouteIdSet) {
        this.arguments = arguments;
        this.authRouteIdSet = authRouteIdSet;
    }
    @Override
    public List<Module> getGuiceModules() {
        return List.of(new Auth0Module(arguments, authRouteIdSet));
    }

    @Override
    public List<Routes> getRouteModules() {
        return List.of(new Auth0Routes(authRouteIdSet));
    }
}
