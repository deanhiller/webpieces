package webpiecesxxxxxpackage.base;

import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.microsvc.api.MicroSvcHeader;
import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.util.context.PlatformHeaders;
import webpiecesxxxxxpackage.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HeadersCtx implements HeaderCtxList {

    public static ClientServiceConfig createConfig(String appName) {
        return new ClientServiceConfig(new HeadersCtx(), appName);
    }

    public List<PlatformHeaders> listHeaderCtxPairs() {
        List<PlatformHeaders> list = new ArrayList<>();
        CompanyHeaders[] values = CompanyHeaders.values();
        MicroSvcHeader[] values1 = MicroSvcHeader.values();
        list.addAll(Arrays.asList(values));
        list.addAll(Arrays.asList(values1));
        return list;
    }
}
