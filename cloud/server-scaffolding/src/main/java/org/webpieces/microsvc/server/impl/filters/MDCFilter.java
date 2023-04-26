package org.webpieces.microsvc.server.impl.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.microsvc.server.api.HeaderCtxList;
import org.webpieces.plugin.json.JacksonCatchAllFilter;
import org.webpieces.plugin.json.MDCHolder;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.FutureHelper;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MDCFilter adds information to the logs. This filter is strictly for logging
 */
public class MDCFilter extends RouteFilter<Void> {

    private FutureHelper futureUtil;
    private HeaderCtxList headerCollector;

    @Inject
    public MDCFilter(FutureHelper futureUtil, ClientServiceConfig config) {
        this.futureUtil = futureUtil;
        headerCollector = config.getHcl();
    }

    @Override
    public void initialize(Void v) {
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        printPreRequestLog(meta);

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

        MDCHolder holder = Context.get(JacksonCatchAllFilter.MDC_INFO);
        holder.setMDCMap(MDC.getCopyOfContextMap());

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

    protected void printPreRequestLog(MethodMeta meta) {

        Method method = meta.getLoadedController().getControllerMethod();
        RouterRequest request = meta.getCtx().getRequest();

        // Use this fancifully-named logger so that you can filter these out by controller method in your logging config
        final Logger preRequestLog = LoggerFactory.getLogger(getClass().getSimpleName() + "." +
                method.getDeclaringClass().getName() + "." + method.getName());

        String httpMethod = request.method.getCode();
        String endpoint = httpMethod + " " + request.domain + ":" + request.port + request.relativePath;
        List<String> headers = meta.getCtx().getRequest().originalRequest.getHeaders().stream()
                .map(h -> h.getName() + ": " + h.getValue())
                .collect(Collectors.toList());
        String json = new String(request.body.createByteArray());

        preRequestLog.info(endpoint+":\n\n"
                + "Headers: "+headers+"\n\n"
                + "Request Body JSON:\n"+json+"\n\n"
                + "The following log is the original request body and its headers. If this log is spammy or "
                + "unnecessary you can disable it in your logging config by filtering out this logger: "+preRequestLog.getName()
        );

    }
}
