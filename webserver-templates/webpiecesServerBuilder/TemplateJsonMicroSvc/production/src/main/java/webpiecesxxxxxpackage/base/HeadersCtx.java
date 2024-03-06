package webpiecesxxxxxpackage.base;

import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.util.context.PlatformHeaders;

import java.util.List;

public class HeadersCtx implements HeaderCtxList {

    public static ClientServiceConfig createConfig(String appName) {
        return new ClientServiceConfig(new HeadersCtx(), appName);
    }

    public List<PlatformHeaders> listHeaderCtxPairs() {
        return null;
    }
}
