package org.webpieces.microsvc.server.impl.filters;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.ctx.api.Current;
import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.microsvc.server.api.HeaderTranslation;
import org.webpieces.recorder.impl.TestCaseHolder;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.util.*;

public class HeaderToRequestStateFilter extends RouteFilter<Void> {

    private static final Logger log = LoggerFactory.getLogger(HeaderToRequestStateFilter.class);
    private final Set<PlatformHeaders> transferKeys = new HashSet<>();

    @Inject
    public HeaderToRequestStateFilter(
            HeaderTranslation translation
    ) {
        List<PlatformHeaders> headers = translation.getHeaders();

        for (PlatformHeaders contextKey : headers) {
            if (!contextKey.isWantTransferred())
                continue;
            transferKeys.add(contextKey);
        }
    }

    @Override
    public void initialize(Void v) {

    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        for (PlatformHeaders contextKey : transferKeys) {
            List<Http2Header> values = Current.request().originalRequest.getHeaderLookupStruct().getHeaders(contextKey.getHeaderName());

            if ((values == null) || values.isEmpty()) {
                continue;
            }

            if (values.size() > 1) {
                log.warn("Skipping header " + contextKey + ": multiple values (" + values + ")");
                continue;
            }

            String value = values.get(0).getValue();

            if (value != null) {
                Context.putMagic(contextKey, value);
            }
        }

        try {
            return nextFilter.invoke(meta);
        } finally {
            for(PlatformHeaders key : transferKeys) {
                Context.removeMagic(key);
            }
        }

    }

}
