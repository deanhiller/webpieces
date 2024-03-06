package org.webpieces.microsvc.server.impl.filters;

import org.slf4j.MDC;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.microsvc.server.api.HeaderTranslation;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * MDCFilter adds information to the logs. This filter is strictly for logging
 */
public class MDCFilter extends RouteFilter<Void> {

    private FutureHelper futureUtil;

    private List<PlatformHeaders> loggerHeaders = new ArrayList<>();

    @Inject
    public MDCFilter(
            FutureHelper futureUtil,
            HeaderTranslation translation
    ) {
        this.futureUtil = futureUtil;
        List<PlatformHeaders> headers = translation.getHeaders();

        for(PlatformHeaders h : headers) {
            if(h.isWantLogged()) {
                loggerHeaders.add(h);
            }
        }
    }

    @Override
    public void initialize(Void v) {
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        Set<String> keys = new HashSet<>();
        try {
            for (PlatformHeaders key : loggerHeaders) {
                String magic = Context.getMagic(key);
                if (magic != null) {
                    MDC.put(key.getLoggerMDCKey(), magic);
                    keys.add(key.getLoggerMDCKey());
                }
            }

            return nextFilter.invoke(meta);

        } finally {
            for(String key : keys) {
                MDC.remove(key);
            }
        }
    }

    private void clearMDC(List<PlatformHeaders> headersCtx) {

        for(PlatformHeaders key : headersCtx) {
            if(!key.isWantLogged()) {
                continue;
            }
            MDC.put(key.getLoggerMDCKey(), null);
        }

    }


}
