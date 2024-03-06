package org.webpieces.microsvc.api;

import org.webpieces.util.context.AddPlatformHeaders;
import org.webpieces.util.context.PlatformHeaders;

public class AddMicroSvcHeaders implements AddPlatformHeaders  {
    @Override
    public Class<? extends PlatformHeaders> platformHeadersToAdd() {
        return MicroSvcHeader.class;
    }
}
