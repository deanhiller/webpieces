package org.webpieces.webserver.routing.app;

import org.webpieces.router.impl.routebldr.CurrentRoutes;
import org.webpieces.router.impl.routebldr.NextPhase;
import org.webpieces.router.impl.routebldr.ProcessCors;

import java.util.List;

public class AllHeadersRestrictDomains implements ProcessCors {

//    //Allow TWO domains for all the routes doing cors here
//        CurrentRoutes.setAllowedCorsDomain(List.of(DOMAIN1, DOMAIN_WITH_PORT));
//        CurrentRoutes.setAllowedCorsHeaders(List.of("*"));
    @Override
    public Object processCors(Object request, NextPhase nextPhase) {
        throw new UnsupportedOperationException("not yet");
    }
}
