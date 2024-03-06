package org.webpieces.microsvc.server.api;

import com.webpieces.http2.api.dto.highlevel.Http2Request;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2Header;
import com.webpieces.http2.api.dto.lowlevel.lib.Http2HeaderName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.ctx.api.ClientServiceConfig;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.data.api.DataWrapper;
import org.webpieces.httpparser.api.common.KnownHeaderName;
import org.webpieces.plugin.json.JacksonCatchAllFilter;
import org.webpieces.plugin.json.ReportingHolderInfo;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.util.context.Context;
import org.webpieces.util.context.PlatformHeaders;
import org.webpieces.util.filters.Service;
import org.webpieces.util.futures.XFuture;
import org.webpieces.util.security.Masker;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class LogExceptionFilter extends RouteFilter<Void> {

    private static final Logger log = LoggerFactory.getLogger(LogExceptionFilter.class);

    protected static final int UNSECURE_PORT = 80;
    protected static final int SECURE_PORT = 443;

    private IgnoreExceptions exceptionCheck;
    private Masker masker;
    private final Set<String> secureList = new HashSet<>();

    @Inject
    public LogExceptionFilter(
            IgnoreExceptions exceptionCheck,
            Masker masker,
            HeaderTranslation translation
    ) {
        this.exceptionCheck = exceptionCheck;
        this.masker = masker;
        List<PlatformHeaders> listHeaders = translation.getHeaders();

        for(PlatformHeaders header : listHeaders) {
            if(header.isSecured()) {
                secureList.add(header.getHeaderName());
            }
        }
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

        if(preRequestLog.isInfoEnabled()) {
            String s = printPreRequestLog(meta);
            String logMsg = s + "The following log is the original request body and its headers. If this log is spammy or "
                    + "unnecessary you can disable it in your logging config by filtering out this logger: " + preRequestLog.getName();
            preRequestLog.info(logMsg);
        }

        long start = System.currentTimeMillis();
        return nextFilter.invoke(meta)
                .handle((resp, e) -> record(preRequestLog, meta, resp, e, start))
                .thenCompose(Function.identity());
    }



    private XFuture<Action> record(Logger preRequestLog, MethodMeta meta, Action resp, Throwable e, long start) {

        ReportingHolderInfo holder = Context.get(JacksonCatchAllFilter.REPORTING_INFO);
        if(holder != null) {
            holder.setReportedException(true);
        }

        long total = System.currentTimeMillis()-start;
        if (e == null) {
            preRequestLog.info("Call to method complete(success). time="+total+"ms");
            return XFuture.completedFuture(resp);
        } else if (exceptionCheck.exceptionIsSuccess(e)) {
            preRequestLog.info("Call to method complete(success exception like BadClientRequestException) time="
                    +total+"ms.  "+ getOrigRequest(preRequestLog, meta), e);
            //still return the failed exception..
            return XFuture.failedFuture(e);
        }

        String errorMsg = "Exception for request(time="+total+"ms). "+getOrigRequest(preRequestLog, meta);
        log.error(errorMsg, e);

        return XFuture.failedFuture(e);
    }

    private String getOrigRequest(Logger preRequestLog, MethodMeta meta) {
        String errorMsg;
        if(!preRequestLog.isInfoEnabled()) {
            //if info is not enabled for prerequest log, add the original request as it was not logged
            errorMsg = "Original req=\n"+printPreRequestLog(meta);
        } else {
            errorMsg = "Follow trace id for original request";
        }
        return errorMsg;
    }

    protected String printPreRequestLog(MethodMeta meta) {
        RouterRequest request = meta.getCtx().getRequest();

//        String httpMethod = request.method.getCode();
//        String endpoint = httpMethod + " " + request.domain + ":" + request.port + request.relativePath;
//        List<String> headers = meta.getCtx().getRequest().originalRequest.getHeaders().stream()
//                .map(h -> h.getName() + ": " + h.getValue())
//                .collect(Collectors.toList());
//        String json = new String(request.body.createByteArray());
//
//        String msg = endpoint+":\n\n"
//                + "Headers: "+headers+"\n\n"
//                + "Request Body JSON:\n"+json+"\n\n";

        Http2Request originalRequest = meta.getCtx().getRequest().originalRequest;
        return createCurl(originalRequest, request.body, request.port);
    }

    private String createCurl(Http2Request req, DataWrapper data, int port) {
        String body = data.createStringFromUtf8(0, data.getReadableSize());

        return createCurl2(port, req, () -> ("--data '" + body + "'"));
    }


    private String createCurl2(int port, Http2Request req, Supplier<String> supplier) {

        String s = "";

        s += "\n\n*********HTTP REQUEST RECEIVED****************************\n";
        s += "         "+req.getMethodString()+" "+req.getPath()+"\n";
        s += "***************************************************************\n";

        s += "curl -k --request " + req.getKnownMethod().getCode() + " ";
        for (Http2Header header : req.getHeaders()) {

            if (header.getName().startsWith(":")) {
                continue; //base headers we can discard
            }

            if(secureList.contains(header.getName())) {
                s += "-H \"" + header.getName() + ":" + masker.maskSensitiveData(header.getValue()) + "\" ";
            } else {
                s += "-H \"" + header.getName() + ":" + header.getValue() + "\" ";
            }

        }

        final String hostHeader = (req.getSingleHeaderValue(Http2HeaderName.AUTHORITY).endsWith(":"+UNSECURE_PORT) ||
                req.getSingleHeaderValue(Http2HeaderName.AUTHORITY).endsWith(":"+SECURE_PORT)) ?
                req.getSingleHeaderValue(Http2HeaderName.AUTHORITY).split(":")[0] : req.getSingleHeaderValue(Http2HeaderName.AUTHORITY);

        s += "-H \"" + KnownHeaderName.HOST + ":" + hostHeader + "\" ";

        String host = req.getSingleHeaderValue(Http2HeaderName.AUTHORITY);
        String path = req.getSingleHeaderValue(Http2HeaderName.PATH);

        s += supplier.get();
        s += " \"https://" + host + path + "\"\n";
        s += "***************************************************************\n";

        return s;

    }
}
