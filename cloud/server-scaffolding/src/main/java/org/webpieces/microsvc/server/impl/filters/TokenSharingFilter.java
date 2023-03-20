package org.webpieces.microsvc.server.impl.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.http.exception.UnauthorizedException;
import org.webpieces.microsvc.api.MicroSvcHeader;
import org.webpieces.microsvc.server.api.FiltersConfig;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;

public class TokenSharingFilter extends RouteFilter<Void> {

    private static final Logger log = LoggerFactory.getLogger(TokenSharingFilter.class);
    private FiltersConfig filtersConfig;

    @Inject
    public TokenSharingFilter(FiltersConfig filtersConfig) {
        this.filtersConfig = filtersConfig;
    }

    @Override
    public void initialize(Void nothing) {
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
        String tokenSharing = filtersConfig.getToken();
        //Fail all requests if there is no http header X-Tray-Brand
        RouterRequest request = meta.getCtx().getRequest();
        String requestHeader = request.getSingleHeaderValue(MicroSvcHeader.SECURE_TOKEN.getHeaderName());
        log.info("TokenFilter checkHeaderName: "+MicroSvcHeader.SECURE_TOKEN.getHeaderName()
                +", request: "+request+", requestHeader: "+requestHeader);
        if(requestHeader == null) {
            log.error("There is no http header");
            throw new UnauthorizedException();
        } else if(!tokenSharing.equals(requestHeader)) {
            log.error("do not have the same Token Sharing");
            throw new UnauthorizedException();
        }
        log.info("TokenFilter invoke(meta) {}", meta);
        return nextFilter.invoke(meta);
    }
}
