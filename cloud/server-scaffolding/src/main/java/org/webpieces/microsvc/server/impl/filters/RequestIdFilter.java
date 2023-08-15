package org.webpieces.microsvc.server.impl.filters;

import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.microsvc.api.MicroSvcHeader;
import org.webpieces.microsvc.server.impl.RequestIdGenerator;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;

public class RequestIdFilter extends RouteFilter<Void> {

    private final RequestIdGenerator requestIdGenerator;
    private final String svcName;

    @Inject
    public RequestIdFilter(RequestIdGenerator requestIdGenerator, ClientServiceConfig config) {
        this.requestIdGenerator = requestIdGenerator;
        this.svcName = config.getServersName();
    }

    @Override
    public void initialize(Void v) {

    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
        RouterRequest request = meta.getCtx().getRequest();

        Context.putMagic(MicroSvcHeader.REQUEST_PATH, request.relativePath);
        String existingReqId = Context.getMagic(MicroSvcHeader.REQUEST_ID);

        if(existingReqId == null) {
            //prepent svcName so we know who generated it for debugging ->
            String requestId = svcName+"-"+requestIdGenerator.generate().toString();
            Context.putMagic(MicroSvcHeader.REQUEST_ID, requestId);
        }

        try {
            return nextFilter.invoke(meta);
        } finally {
            Context.removeMagic(MicroSvcHeader.REQUEST_PATH);
            if(existingReqId == null) {
                //only clear magic if we set the magic in this filter in the first place
                Context.removeMagic(MicroSvcHeader.REQUEST_ID);
            }
        }
    }

}
