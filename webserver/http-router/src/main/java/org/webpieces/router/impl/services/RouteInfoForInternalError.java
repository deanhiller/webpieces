package org.webpieces.router.impl.services;

public class RouteInfoForInternalError implements RouteData {

    private boolean forceEndOfStream;

    public RouteInfoForInternalError(boolean forceEndOfStream) {
        this.forceEndOfStream = forceEndOfStream;
    }

    public boolean isForceEndOfStream() {
        return forceEndOfStream;
    }
}
