package org.webpieces.microsvc.client.api;

import org.webpieces.util.context.AddPlatformHeaders;
import org.webpieces.util.context.PlatformHeaders;

public class AddGcpAuthHeaders implements AddPlatformHeaders {
    @Override
    public Class<? extends PlatformHeaders> platformHeadersToAdd() {
        return GcpAuthHeader.class;
    }

}
