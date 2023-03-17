package org.webpieces.microsvc.server.impl.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.http.exception.UnauthorizedException;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

public class TokenSharingFilter extends RouteFilter<String> {

    private static final Logger log = LoggerFactory.getLogger(TokenSharingFilter.class);

    private final String tokenSharing;
    private String checkHeaderName;

    public TokenSharingFilter(String tokenSharing) {
        this.tokenSharing = tokenSharing;
    }

    @Override
    public void initialize(String checkHeaderName) {
        this.checkHeaderName = checkHeaderName;
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
        //Fail all requests if there is no http header X-Tray-Brand
        RouterRequest request = meta.getCtx().getRequest();
        String requestHeader = request.getSingleHeaderValue(checkHeaderName);
        log.info("TokenFilter checkHeaderName: {}, request: {}, requestHeader: {}", this.checkHeaderName, request, requestHeader);
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
