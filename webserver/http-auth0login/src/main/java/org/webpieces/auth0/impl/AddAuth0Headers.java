package org.webpieces.auth0.impl;

import org.webpieces.util.context.AddPlatformHeaders;
import org.webpieces.util.context.PlatformHeaders;

public class AddAuth0Headers implements AddPlatformHeaders {
    @Override
    public Class<? extends PlatformHeaders> platformHeadersToAdd() {
        return Auth0Header.class;
    }

}
