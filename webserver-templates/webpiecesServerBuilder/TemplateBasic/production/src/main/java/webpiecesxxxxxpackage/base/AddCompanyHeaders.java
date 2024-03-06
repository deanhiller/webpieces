package webpiecesxxxxxpackage.base;

import org.webpieces.util.context.AddPlatformHeaders;
import org.webpieces.util.context.PlatformHeaders;

public class AddCompanyHeaders implements AddPlatformHeaders {
    @Override
    public Class<? extends PlatformHeaders> platformHeadersToAdd() {
        return CompanyHeaders.class;
    }
}
