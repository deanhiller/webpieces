package org.webpieces.microsvc.server.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.plugin.json.JacksonCatchAllFilter;
import org.webpieces.plugin.json.ReportingHolderInfo;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class LogExceptionFilter extends RouteFilter<Void> {

    private static final Logger log = LoggerFactory.getLogger(LogExceptionFilter.class);

    private IgnoreExceptions exceptionCheck;

    @Inject
    public LogExceptionFilter(IgnoreExceptions exceptionCheck) {
        this.exceptionCheck = exceptionCheck;
    }

    @Override
    public void initialize(Void initialConfig) {

    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
        Method method = meta.getLoadedController().getControllerMethod();

        // Use this fancifully-named logger so that you can filter these out by controller method in your logging config
        final Logger preRequestLog = LoggerFactory.getLogger(getClass().getSimpleName() + "." +
                method.getDeclaringClass().getName() + "." + method.getName());

        String s = printPreRequestLog(meta);
        String logMsg = s+ "The following log is the original request body and its headers. If this log is spammy or "
                + "unnecessary you can disable it in your logging config by filtering out this logger: "+preRequestLog.getName();
        preRequestLog.info(logMsg);

        return nextFilter.invoke(meta)
                .handle((resp, e) -> record(preRequestLog, meta, resp, e))
                .thenCompose(Function.identity());
    }



    private XFuture<Action> record(Logger preRequestLog, MethodMeta meta, Action resp, Throwable e) {

        if (e == null) {
            log.debug("Call to method complete");
            return XFuture.completedFuture(resp);
        } else if (exceptionCheck.exceptionIsSuccess(e)) {
            return XFuture.completedFuture(resp);
        }

        String msg = "";
        if(!preRequestLog.isInfoEnabled()) {
            //if info is not enabled for prerequest log, add the original request as it was not logged
            msg = printPreRequestLog(meta);
        }

        ReportingHolderInfo holder = Context.get(JacksonCatchAllFilter.REPORTING_INFO);
        holder.setReportedException(true);
        log.error("Exception for request=\n"+msg, e);

        return XFuture.failedFuture(e);
    }

    protected String printPreRequestLog(MethodMeta meta) {
        RouterRequest request = meta.getCtx().getRequest();

        String httpMethod = request.method.getCode();
        String endpoint = httpMethod + " " + request.domain + ":" + request.port + request.relativePath;
        List<String> headers = meta.getCtx().getRequest().originalRequest.getHeaders().stream()
                .map(h -> h.getName() + ": " + h.getValue())
                .collect(Collectors.toList());
        String json = new String(request.body.createByteArray());

        String msg = endpoint+":\n\n"
                + "Headers: "+headers+"\n\n"
                + "Request Body JSON:\n"+json+"\n\n";

        return msg;
    }
}
