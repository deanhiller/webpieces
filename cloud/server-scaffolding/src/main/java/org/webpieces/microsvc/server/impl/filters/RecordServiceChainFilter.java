package org.webpieces.microsvc.server.impl.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.http.exception.BadRequestException;
import org.webpieces.microsvc.api.MicroSvcHeader;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.util.function.Supplier;

public class RecordServiceChainFilter extends RouteFilter<Void> {

    private static final Logger log = LoggerFactory.getLogger(RecordServiceChainFilter.class);
    private final String serviceName;

    @Inject
    public RecordServiceChainFilter(ClientServiceConfig config) {
        serviceName = config.getServiceName();
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
        String magic = Context.getMagic(MicroSvcHeader.FILTER_CHAIN);
        log.info("RecordServiceChainFilter checkHeaderName: "+magic);

        //Will NOT match the last service name intentionally allowing a service to call itself
        if(magic.contains(":"+serviceName+":")) {
            throw new BadRequestException("There is a cycle in your architecture that is not allowed");
        }

        if(magic == null) {
            magic = ":"+serviceName;
        } else {
            magic = magic + ":" +serviceName;
        }

        Context.putMagic(MicroSvcHeader.FILTER_CHAIN, magic);
        log.info("RecordServiceChainFilter invoke(meta) {}", meta);
        return nextFilter.invoke(meta);
    }

    @Override
    public void initialize(Void initialConfig) {

    }
}
