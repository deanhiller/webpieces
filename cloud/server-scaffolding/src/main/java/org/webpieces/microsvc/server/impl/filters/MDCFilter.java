package org.webpieces.microsvc.server.impl.filters;

import org.slf4j.MDC;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.util.List;

/**
 * MDCFilter adds information to the logs. This filter is strictly for logging
 */
public class MDCFilter extends RouteFilter<HeaderCtxList> {

    private FutureHelper futureUtil;
    private HeaderCtxList headerCollector;

    @Inject
    public MDCFilter(FutureHelper futureUtil) {
        this.futureUtil = futureUtil;
    }

    @Override
    public void initialize(HeaderCtxList headerCollector) {
        this.headerCollector = headerCollector;
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        RouterRequest routerReq = meta.getCtx().getRequest();

        List<PlatformHeaders> headersCtx = headerCollector.listHeaderCtxPairs();
        for(PlatformHeaders key : headersCtx) {
            if(!key.isWantLogged()) {
                continue;
            }

            String magic = Context.getMagic(key);
            if(magic != null) {
                MDC.put(key.getLoggerMDCKey(), magic);
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
