package org.webpieces.googleauth.impl;

import org.webpieces.util.context.AddPlatformHeaders;
import org.webpieces.util.context.PlatformHeaders;

public class AddAuthHeader implements AddPlatformHeaders {
    @Override
    public Class<? extends PlatformHeaders> platformHeadersToAdd() {
        return AuthHeader.class;
    }
    
}
