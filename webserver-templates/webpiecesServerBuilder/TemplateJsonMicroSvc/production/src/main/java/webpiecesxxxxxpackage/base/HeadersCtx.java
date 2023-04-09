package webpiecesxxxxxpackage.base;

import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.util.context.PlatformHeaders;
import webpiecesxxxxxpackage.Server;

import java.util.ArrayList;
import java.util.List;

public class HeadersCtx implements HeaderCtxList {
    public static ClientServiceConfig createConfig(String svcName) {
        return new ClientServiceConfig(new HeadersCtx(), svcName);
    }

    @Override
    public List<PlatformHeaders> listHeaderCtxPairs() {
        return new ArrayList<>();
    }
}
