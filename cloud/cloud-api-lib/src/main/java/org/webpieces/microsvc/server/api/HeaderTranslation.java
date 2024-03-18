package org.webpieces.microsvc.server.api;

import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.util.context.AddPlatformHeaders;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;

import javax.inject.Inject;
import java.util.*;

public class HeaderTranslation {

    private final List<PlatformHeaders> listHeaders;

    @Inject
    public HeaderTranslation(
            Set<AddPlatformHeaders> addPlatformHeaders,
            ClientServiceConfig config
    ) {
        ArrayList<PlatformHeaders> initialList = new ArrayList<>();
        for(AddPlatformHeaders add : addPlatformHeaders) {
            Class<? extends PlatformHeaders> clazz = add.platformHeadersToAdd();
            PlatformHeaders[] enumConstants = clazz.getEnumConstants();
            List<PlatformHeaders> list = Arrays.asList(enumConstants);
            initialList.addAll(list);
        }

        listHeaders = checkForDuplicates(initialList);
    }

    public List<PlatformHeaders> getHeaders() {
        return listHeaders;
    }

    /**
     * We use header as the key unless null and then use mdc as the key.  Do not allow duplication
     * including mdc can't use a header name if used as the key.
     */
    public List<PlatformHeaders> checkForDuplicates(List<PlatformHeaders> platformHeaders) {
        List<PlatformHeaders> normalizedList = new ArrayList<>();

        Map<String, PlatformHeaders> headerKeyToHeader = new HashMap<>();
        Map<String, PlatformHeaders> mdcKeyToHeader = new HashMap<>();

        for(PlatformHeaders header : platformHeaders) {
            if(header.getHeaderName() == null && header.getLoggerMDCKey() == null) {
                throw new IllegalArgumentException("Either header or MDC must contain a value.  both cannot be null");
            }
            PlatformHeaders existingFromHeader = null;
            if(header.getHeaderName() != null)
                existingFromHeader = headerKeyToHeader.get(header.getHeaderName());
            PlatformHeaders existingFromMdc = null;
            if(header.getLoggerMDCKey() != null)
                existingFromMdc = mdcKeyToHeader.get(header.getLoggerMDCKey());

            if(existingFromHeader != null) {
                if(header.getLoggerMDCKey() != existingFromHeader.getLoggerMDCKey())
                    throw new IllegalStateException("header="+tuple(header)+" and header="+tuple(existingFromHeader)+" define the same header " +
                            "but they define getLoggerMDCKey differently.  remove one of the plugins or modules to remove one of these headers or redine th header to match");
                compareHeader(header, existingFromHeader);
                continue; // no need to add duplicate, they are the same
            } else if(existingFromMdc != null) {
                if(header.getHeaderName() != existingFromMdc.getHeaderName())
                    throw new IllegalStateException("header="+tuple(header)+" and header="+tuple(existingFromMdc)+" define the same mdc key " +
                            "but they define getHeaderName() differently.  remove one of the plugins or modules to remove one of these headers or redine th header to match");
                compareHeader(header, existingFromMdc);

                continue; //no need to add duplicate, they are the same
            }

            headerKeyToHeader.put(header.getHeaderName(), header);
            mdcKeyToHeader.put(header.getLoggerMDCKey(), header);

            normalizedList.add(header);
        }

        return normalizedList;
    }

    private void compareHeader(PlatformHeaders header, PlatformHeaders existingFromHeader) {
        if(header.isWantLogged() != existingFromHeader.isWantLogged()
                || header.isDimensionForMetrics() != existingFromHeader.isDimensionForMetrics()
                || header.isSecured() != existingFromHeader.isSecured()
                || header.isWantTransferred() != existingFromHeader.isWantTransferred()
        )
            throw new IllegalStateException("header="+tuple(header)+" and header="+tuple(existingFromHeader)+" define the same header " +
                    "but they define their properties differently.  remove one of the plugins or modules to remove one of these headers or redine th header to match");
    }

    private static String tuple(PlatformHeaders header) {
        return header.getClass()+"."+header;
    }

}
