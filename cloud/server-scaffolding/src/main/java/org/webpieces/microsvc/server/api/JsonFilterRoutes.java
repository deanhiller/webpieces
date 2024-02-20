package org.webpieces.microsvc.server.api;

import org.webpieces.router.api.routes.Routes;

/**
 * Switch to FilterRoutes which is used by web and json now
 */
@Deprecated
public class JsonFilterRoutes extends FilterRoutes implements Routes {

    public JsonFilterRoutes(FilterConfig config) {
        super(config);
    }

}
