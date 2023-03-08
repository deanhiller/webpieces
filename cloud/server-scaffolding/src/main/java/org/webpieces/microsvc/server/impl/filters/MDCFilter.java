package org.webpieces.microsvc.server.impl.filters;

import org.slf4j.MDC;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.PlatformHeaders;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.util.List;

/**
 * MDCFilter adds information to the logs. This filter is strictly for logging
 */
public class MDCFilter extends RouteFilter<Void> {

    private FutureHelper futureUtil;
    private HeaderCtxList headerCollector;

    @Inject
    public MDCFilter(FutureHelper futureUtil, HeaderCtxList headerCollector) {
        this.futureUtil = futureUtil;
        this.headerCollector = headerCollector;
    }

    @Override
    public void initialize(Void initialConfig) {

    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        RouterRequest routerReq = meta.getCtx().getRequest();

        List<PlatformHeaders> headersCtx = headerCollector.listHeaderCtxPairs();
        for(PlatformHeaders key : headersCtx) {
            if(!key.isWantLogged()) {
                continue;
            }
            Object value = routerReq.getRequestState(key);
            if(value != null) {
                MDC.put(key.getLoggerMDCKey(), String.valueOf(value));
            }
        }

        return futureUtil.finallyBlock(
                () -> nextFilter.invoke(meta),
                () -> clearMDC(headersCtx)
        );

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
