package org.webpieces.router.impl.routebldr;


public interface ProcessCors {

    /**
     * @param request Http2Request if the router is in webpieces else other platforms request
     * @return Http2Response going back
     */
    public Object processCors(Object request, NextPhase nextPhase);

}
