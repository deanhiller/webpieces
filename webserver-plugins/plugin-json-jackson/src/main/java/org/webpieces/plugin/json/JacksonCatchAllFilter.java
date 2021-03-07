package org.webpieces.plugin.json;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.ConstraintViolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.exceptions.BadClientRequestException;
import org.webpieces.router.api.exceptions.HttpException;
import org.webpieces.router.api.exceptions.Violation;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.util.filters.Service;

import com.webpieces.http2.api.dto.lowlevel.StatusCode;

@Singleton
public class JacksonCatchAllFilter extends RouteFilter<JsonConfig> {

	static final String JSON_REQUEST_KEY = "_jsonRequest";
	private static final Logger log = LoggerFactory.getLogger(JacksonCatchAllFilter.class);
	public static final MimeTypeResult MIME_TYPE = new MimeTypeResult("application/json", StandardCharsets.UTF_8);
	private final JacksonJsonConverter mapper;

	private Pattern pattern;

	@Inject
	public JacksonCatchAllFilter(JacksonJsonConverter mapper) {
		this.mapper = mapper;
	}
	
	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		return nextFilter.invoke(meta).handle((a, t) -> translateFailure(meta, a, t));
	}

	@Override
	public void initialize(JsonConfig config) {
		this.pattern = config.getFilterPattern();
	}

	protected Action translateFailure(MethodMeta meta, Action action, Throwable t) {
		if(t != null) {
			if(t instanceof HttpException) {
				return translate(meta, (HttpException)t);
			}
			
			byte[] obj = (byte[]) meta.getCtx().getRequest().requestState.get(JSON_REQUEST_KEY);
			String json = new String(obj, 0, Math.max(100, obj.length));
			log.error("Request failed for json="+json+"\nInternal Server Error method="+meta.getLoadedController().getControllerMethod(), t);
			return translateError(t);
		} else {
			return action;
		}
	}

	protected Action translate(MethodMeta meta, HttpException t) {
		byte[] content = translateHttpException(meta, t);
		StatusCode status = t.getStatusCode();
		String msg = "Error";
		int code = t.getHttpCode();
		if(status != null)
			msg = status.getReason();
		return new RenderContent(content, code, msg, MIME_TYPE);		
	}

	protected RenderContent translateError(Throwable t) {
		byte[] content = translateServerError(t);
		KnownStatusCode status = KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR;
		return new RenderContent(content, status.getCode(), status.getReason(), MIME_TYPE);
	}

	protected CompletableFuture<Action> createNotFoundResponse(Service<MethodMeta, Action> nextFilter, MethodMeta meta) {
		Matcher matcher = pattern.matcher(meta.getCtx().getRequest().relativePath);
		if(!matcher.matches())
			return nextFilter.invoke(meta);
		
		return CompletableFuture.completedFuture(
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
		if(statusCode != null) {
			String message = t.getStatusCode().getReason()+" : "+t.getMessage();
			if(t instanceof BadClientRequestException) {
				message = translateViolations((BadClientRequestException) t, message);
			}
			error.setError(message);
			error.setCode(t.getStatusCode().getCode());
		} else {
			error.setCode(t.getHttpCode());
		}

		if(log.isDebugEnabled()) {
			byte[] obj = (byte[]) meta.getCtx().getRequest().requestState.get(JSON_REQUEST_KEY);
			String json = new String(obj);
			log.debug("Request json failed="+json+"\n"+error.getError());
		}
		
		return translateJson(mapper, error);
	}


	protected String translateViolations(BadClientRequestException t, String defaultMessage) {
		if(t.getViolations() == null || t.getViolations().size() == 0) {
			return defaultMessage;
		}
		
		String failures = "Your request is bad. ";
		int counter = 1;
		for(Violation violation : t.getViolations()) {
			failures += "Violation #"+counter+":'"+violation.getMessage()+"' path="+violation.getPath();
			if(counter < t.getViolations().size()) {
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
