package org.webpieces.microsvc.server.api;

import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.util.context.AddPlatformHeaders;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class HeaderTranslation {

    private final List<PlatformHeaders> listHeaders;

    @Inject
    public HeaderTranslation(
            Set<AddPlatformHeaders> addPlatformHeaders,
            ClientServiceConfig config
    ) {
        listHeaders = new ArrayList<>();
        for(AddPlatformHeaders add : addPlatformHeaders) {
            Class<? extends PlatformHeaders> clazz = add.platformHeadersToAdd();
            PlatformHeaders[] enumConstants = clazz.getEnumConstants();
            List<PlatformHeaders> list = Arrays.asList(enumConstants);
            listHeaders.addAll(list);
        }

        Context.checkForDuplicates(listHeaders);
    }

    public List<PlatformHeaders> getHeaders() {
        return listHeaders;
    }

}
