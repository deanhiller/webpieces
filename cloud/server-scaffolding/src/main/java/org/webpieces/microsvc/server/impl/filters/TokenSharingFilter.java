package org.webpieces.microsvc.server.impl.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.http.exception.UnauthorizedException;
import org.webpieces.microsvc.api.MicroSvcHeader;
import org.webpieces.microsvc.server.api.TokenConfig;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;

public class TokenSharingFilter extends RouteFilter<Void> {

    private static final Logger log = LoggerFactory.getLogger(TokenSharingFilter.class);
    private TokenConfig tokenConfig;

    @Inject
    public TokenSharingFilter(TokenConfig tokenConfig) {
        this.tokenConfig = tokenConfig;
    }

    @Override
    public void initialize(Void nothing) {
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
        String tokenSharing = tokenConfig.getToken();
        //Fail all requests if there is no secure token
        String token = Context.getMagic(MicroSvcHeader.SECURE_TOKEN);
        log.debug("TokenFilter checkHeaderName: "+MicroSvcHeader.SECURE_TOKEN.getHeaderName());
        if(!tokenSharing.equals(token)) {
            log.info("do not have the same Token Sharing");
            throw new UnauthorizedException();
        }
        return nextFilter.invoke(meta);
    }
}
