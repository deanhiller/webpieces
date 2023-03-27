package org.webpieces.microsvc.server.impl.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.http.exception.UnauthorizedException;
import org.webpieces.microsvc.api.MicroSvcHeader;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

public class FilterChain extends RouteFilter<Void> {

    private static final Logger log = LoggerFactory.getLogger(FilterChain.class);

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
        RouterRequest request = meta.getCtx().getRequest();
        String requestHeader = request.getSingleHeaderValue(MicroSvcHeader.FILTER_CHAIN.getHeaderName());
        log.info("FilterChain checkHeaderName: "+MicroSvcHeader.FILTER_CHAIN.getHeaderName() +", request: "+request+", requestHeader: "+requestHeader);
        String chain;

        if(requestHeader != null) {
            chain = requestHeader;
        } else {
            chain = "???"; //requestHeader + "-" + "????";
        }

        Context.putMagic(MicroSvcHeader.FILTER_CHAIN, chain);
        log.info("FilterChain invoke(meta) {}", meta);
        return nextFilter.invoke(meta);
    }

    @Override
    public void initialize(Void initialConfig) {

    }
}
