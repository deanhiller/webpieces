package org.webpieces.plugins.json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.exceptions.AuthenticationException;
import org.webpieces.router.api.exceptions.AuthorizationException;
import org.webpieces.router.api.exceptions.ClientDataError;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.util.filters.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class JacksonCatchAllFilter extends RouteFilter<JsonConfig> {

	private static final Logger log = LoggerFactory.getLogger(JacksonCatchAllFilter.class);
	public static final MimeTypeResult MIME_TYPE = new MimeTypeResult("application/json", StandardCharsets.UTF_8);
	private final ObjectMapper mapper;

	private Boolean isNotFoundFilter;
	private Pattern pattern;

	public JacksonCatchAllFilter(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		if(isNotFoundFilter)
			return createNotFoundResponse(nextFilter, meta);

		return nextFilter.invoke(meta).handle((a, t) -> translateFailure(a, t));
	}

	@Override
	public void initialize(JsonConfig config) {
		this.isNotFoundFilter = config.isNotFoundFilter();
		this.pattern = config.getFilterPattern();
	}

	protected Action translateFailure(Action action, Throwable t) {
		if(t != null) {
			if(t instanceof ClientDataError) {
				return translate((ClientDataError) t);
			} else if(t instanceof AuthorizationException) {
				return translate((AuthorizationException)t);
			} else if(t instanceof AuthenticationException) {
				return translate((AuthenticationException) t);
			} else if (t instanceof NotFoundException) {
				return createNotFound();
			}
			
			log.error("Internal Server Error", t);
			return translateError(t);
		} else {
			return action;
		}
	}

	protected Action translate(AuthorizationException t) {
		byte[] content = translateAuthorizationError(t);
		KnownStatusCode status = KnownStatusCode.HTTP_401_UNAUTHORIZED;
		return new RenderContent(content, status.getCode(), status.getReason(), MIME_TYPE);
	}

	protected Action translate(AuthenticationException t) {
		byte[] content = translateAuthenticationError(t);
		KnownStatusCode status = KnownStatusCode.HTTP_403_FORBIDDEN;
		return new RenderContent(content, status.getCode(), status.getReason(), MIME_TYPE);
	}

	protected RenderContent translateError(Throwable t) {
		byte[] content = translateServerError(t);
		KnownStatusCode status = KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR;
		return new RenderContent(content, status.getCode(), status.getReason(), MIME_TYPE);
	}

	protected RenderContent translate(ClientDataError t) {
		byte[] content = translateClientError(t);
		KnownStatusCode status = KnownStatusCode.HTTP_400_BADREQUEST;
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

	protected byte[] translateAuthenticationError(AuthenticationException t) {
		JsonError error = new JsonError();
		error.setError("403 Forbidden: "+t.getMessage());
		error.setCode(403);

		return translateJson(mapper, error);
	}

	protected byte[] translateAuthorizationError(AuthorizationException t) {
		JsonError error = new JsonError();
		error.setError("401 Not Authorized : "+t.getMessage());
		error.setCode(401);

		return translateJson(mapper, error);
	}

	protected byte[] translateClientError(ClientDataError t) {
		JsonError error = new JsonError();
		error.setError("400 bad request: "+t.getMessage());
		error.setCode(400);

		return translateJson(mapper, error);
	}

	protected byte[] createNotFoundJsonResponse() {
		JsonError error = new JsonError();
		error.setError("404 This url does not exist.  try another url");
		error.setCode(404);
		return translateJson(mapper, error);
	}

	protected byte[] translateServerError(Throwable t) {
		JsonError error = new JsonError();
		error.setError("Server ran into a bug, please report");
		error.setCode(500);
		return translateJson(mapper, error);
	}
	
	protected byte[] translateJson(ObjectMapper mapper, Object error) {
		try {
			return mapper.writeValueAsBytes(error);
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
