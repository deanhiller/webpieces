package org.webpieces.plugin.json;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.webpieces.ctx.api.RouterRequest;
import org.webpieces.http.StatusCode;
import org.webpieces.http.exception.BadRequestException;
import org.webpieces.http.exception.HttpException;
import org.webpieces.http.exception.Violation;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.util.context.Context;
import org.webpieces.util.filters.Service;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.webpieces.util.futures.XFuture;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class JacksonCatchAllFilter extends RouteFilter<JsonConfig> {

    private static final Logger log = LoggerFactory.getLogger(JacksonCatchAllFilter.class);
    public static final String REPORTING_INFO = "webpieces-reportingInfoHolder";

    public static final MimeTypeResult MIME_TYPE = new MimeTypeResult("application/json", StandardCharsets.UTF_8);
    private final JacksonJsonConverter mapper;

    private Pattern pattern;

    @Inject
    public JacksonCatchAllFilter(JacksonJsonConverter mapper) {
        this.mapper = mapper;
    }

    @Override
    public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {

        /**
         * Since magic headers come in through http and are set in HeaderToRequestStateFilter.java
         * which is MUCH after this filter, this filter has no context so we give the MDCFilter.java
         * a chance to store the info in MDCHolder so we can read and add that MDC to the logs when
         * requests fail so we can trace through MicroSvcHeaders.REQUEST_ID for example.
         */
        ReportingHolderInfo holder = new ReportingHolderInfo();
        Context.put(REPORTING_INFO, holder);

        printPreRequestLog(meta);

        return nextFilter.invoke(meta).handle((a, t) -> translateFailure(meta, a, t, holder));
    }

    @Override
    public void initialize(JsonConfig config) {

        this.pattern = config.getFilterPattern();

    }

    protected void printPreRequestLog(MethodMeta meta) {
        if(!log.isDebugEnabled())
            return;

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

        preRequestLog.debug(endpoint+":\n\n"
                + "Headers: "+headers+"\n\n"
                + "Request Body JSON:\n"+json+"\n\n"
                + "The following log is the original request body and its headers. If this log is spammy or "
                + "unnecessary you can disable it in your logging config by filtering out this logger: "+preRequestLog.getName()
        );

    }
    protected Action translateFailure(MethodMeta meta, Action action, Throwable t, ReportingHolderInfo holder) {
        if (t != null) {

            byte[] obj = meta.getCtx().getRequest().body.createByteArray();
            String json = new String(obj, 0, Math.min(obj.length, 100));

            if(t instanceof javax.ws.rs.BadRequestException) {
                log.info("Translating javax.ws.rs.BadRequestException");
                t = new BadRequestException(t.getMessage());
            }

            if(t instanceof HttpException) {
                int httpCode = ((HttpException) t).getHttpCode();
                if (httpCode >= 500 && httpCode < 600) {
                    reportException(holder.isReportedException(), "Request failed for json=" + json + "\n500 Internal Server Error method=" + meta.getLoadedController().getControllerMethod(), t);
                }
                return translate(meta, (HttpException)t);
            }

            reportException(holder.isReportedException(), "Request failed for json=" + json + "\nInternal Server Error method=" + meta.getLoadedController().getControllerMethod(), t);
            return translateError(t);

        } else {
            return action;
        }
    }

    private void reportException(boolean reportedException, String errorMsg, Throwable t) {
        if(reportedException)
            return;

        log.error(errorMsg, t);
    }

    protected Action translate(MethodMeta meta, HttpException t) {
        byte[] content = translateHttpException(meta, t);
        StatusCode status = t.getStatusCode();
        String msg = "Error";
        int code = t.getHttpCode();
        if (status != null)
            msg = status.getReason();
        return new RenderContent(content, code, msg, MIME_TYPE);
    }

    protected RenderContent translateError(Throwable t) {
        byte[] content = translateServerError(t);
        KnownStatusCode status = KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR;
        return new RenderContent(content, status.getCode(), status.getReason(), MIME_TYPE);
    }

    protected XFuture<Action> createNotFoundResponse(Service<MethodMeta, Action> nextFilter, MethodMeta meta) {
        Matcher matcher = pattern.matcher(meta.getCtx().getRequest().relativePath);
        if (!matcher.matches())
            return nextFilter.invoke(meta);

        return XFuture.completedFuture(
                createNotFound()
        );
    }

    protected Action createNotFound() {
        byte[] content = createNotFoundJsonResponse();
        return new RenderContent(content, KnownStatusCode.HTTP_404_NOTFOUND.getCode(), KnownStatusCode.HTTP_404_NOTFOUND.getReason(), MIME_TYPE);
    }

    protected byte[] translateHttpException(MethodMeta meta, HttpException t) {
        JsonError error = new JsonError();
        StatusCode statusCode = t.getStatusCode();
        if (statusCode != null) {
            String message = t.getStatusCode().getReason() + " : " + t.getMessage();
            if (t instanceof BadRequestException) {
                message = translateViolations((BadRequestException) t, message);
            }
            error.setError(message);
            error.setCode(t.getStatusCode().getCode());
        } else {
            error.setCode(t.getHttpCode());
        }

        if (log.isDebugEnabled()) {
            byte[] obj = meta.getCtx().getRequest().body.createByteArray();
            String json = new String(obj);
            log.debug("Request json failed=" + json + "\n" + error.getError());
        }

        return translateJson(mapper, error);
    }


    protected String translateViolations(BadRequestException t, String defaultMessage) {
        if (t.getViolations() == null || t.getViolations().size() == 0) {
            return defaultMessage;
        }

        String failures = "Your request is bad. ";
        int counter = 1;
        for (Violation violation : t.getViolations()) {
            failures += "Violation #" + counter + ":'" + violation.getMessage() + "' path=" + violation.getPath();
            if (counter < t.getViolations().size()) {
                failures += "****";
            }
        }

        return failures;
    }

    protected byte[] createNotFoundJsonResponse() {
        JsonError error = new JsonError();
        error.setError("This url does not exist.  try another url");
        error.setCode(404);
        return translateJson(mapper, error);
    }

    protected byte[] translateServerError(Throwable t) {
        JsonError error = new JsonError();
        error.setError("Server ran into a bug, please report");
        error.setCode(500);
        return translateJson(mapper, error);
    }

    protected byte[] translateJson(JacksonJsonConverter mapper, Object error) {
        return mapper.writeValueAsBytes(error);
    }

}
