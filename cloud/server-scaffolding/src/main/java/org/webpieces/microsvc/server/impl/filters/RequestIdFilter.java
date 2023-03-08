package org.webpieces.microsvc.server.impl.filters;

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

public class RequestIdFilter extends RouteFilter<String> {

    private final RequestIdGenerator requestIdGenerator;
    private String svcName;

    @Inject
    public RequestIdFilter(RequestIdGenerator requestIdGenerator) {
        this.requestIdGenerator = requestIdGenerator;
    }

    @Override
    public void initialize(String svcName) {
        this.svcName = svcName;
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        RouterRequest request = meta.getCtx().getRequest();

        Context.putMagic(MicroSvcHeader.REQUEST_PATH, request.relativePath);
        String requestHeader = request.getSingleHeaderValue(MicroSvcHeader.REQUEST_ID.getHeaderName());
        String requestId;

        if(requestHeader != null) {
            requestId = requestHeader;
        } else {
            //prepent svcName so we know who generated it for debugging ->
            requestId = svcName+"-"+requestIdGenerator.generate().toString();
        }

        Context.putMagic(MicroSvcHeader.REQUEST_ID, requestId);

        return nextFilter.invoke(meta);
    }

}
