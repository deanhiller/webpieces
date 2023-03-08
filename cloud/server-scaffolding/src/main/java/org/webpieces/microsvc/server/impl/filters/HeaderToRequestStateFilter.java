package org.webpieces.microsvc.server.impl.filters;

import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.Current;
import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.util.*;

public class HeaderToRequestStateFilter extends RouteFilter<HeaderCtxList> {

    private static final Logger log = LoggerFactory.getLogger(HeaderToRequestStateFilter.class);
    private HeaderCtxList headerCtxList;

    @Inject
    public HeaderToRequestStateFilter() {
    }

    @Override
    public void initialize(HeaderCtxList ctxList) {
        this.headerCtxList = ctxList;
        List<PlatformHeaders> platformHeaders = headerCtxList.listHeaderCtxPairs();
        Context.checkForDuplicates(platformHeaders);
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        //*********************************************
        // Move HeaderCollector to webpieces
        //*****************************************************
        List<PlatformHeaders> headers = headerCtxList.listHeaderCtxPairs();

        for (PlatformHeaders contextKey : headers) {
            if(!contextKey.isWantTransferred())
                continue;

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

        return nextFilter.invoke(meta);

    }

}
