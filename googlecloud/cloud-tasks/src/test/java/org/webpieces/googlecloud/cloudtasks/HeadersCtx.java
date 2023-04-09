package org.webpieces.googlecloud.cloudtasks;

import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.util.context.PlatformHeaders;

import java.util.ArrayList;
import java.util.List;

public class HeadersCtx implements HeaderCtxList {
    @Override
    public List<PlatformHeaders> listHeaderCtxPairs() {
        return new ArrayList<>();
    }
}
