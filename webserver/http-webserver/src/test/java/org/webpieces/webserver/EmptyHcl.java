package org.webpieces.webserver;

import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.util.context.PlatformHeaders;

import java.util.List;

public class EmptyHcl implements HeaderCtxList {
    @Override
    public List<PlatformHeaders> listHeaderCtxPairs() {
        return null;
    }
}
