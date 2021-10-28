package org.webpieces.webserver.routing.app;

import org.webpieces.router.impl.routebldr.CurrentRoutes;
import org.webpieces.router.impl.routebldr.NextPhase;
import org.webpieces.router.impl.routebldr.ProcessCors;

import java.util.List;

public class AllDomainsRestrictHeaders implements ProcessCors {

//        CurrentRoutes.setAllowedCorsDomain(List.of("*"));
//        CurrentRoutes.setAllowedCorsHeaders(List.of("Authorization", "Content-Type"));
    @Override
    public Object processCors(Object request, NextPhase nextPhase) {
        throw new IllegalStateException("not implemented yet");
    }
}
