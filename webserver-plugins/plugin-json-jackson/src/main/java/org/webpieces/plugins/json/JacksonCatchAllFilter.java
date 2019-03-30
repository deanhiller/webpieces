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
import org.webpieces.router.api.exceptions.ClientDataError;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.router.impl.dto.MethodMeta;
import org.webpieces.util.filters.Service;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public abstract class JacksonCatchAllFilter extends RouteFilter<JsonConfig> {

	private static final Logger log = LoggerFactory.getLogger(JacksonCatchAllFilter.class);
	public static final MimeTypeResult MIME_TYPE = new MimeTypeResult("application/json", StandardCharsets.UTF_8);
	private Boolean isNotFoundFilter;
	private Pattern pattern;

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

	private Action translateFailure(Action action, Throwable t) {
		if(t != null) {
			if(t instanceof ClientDataError) {
				return translate((ClientDataError)t);
			} else if (t instanceof NotFoundException) {
				return createNotFound();
			}
			
			log.error("Internal Server Error", t);
			return translateError(t);
		} else {
			return action;
		}
	}

	private RenderContent translateError(Throwable t) {
		byte[] content = translateServerError(t);
		return new RenderContent(content, KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR.getCode(), KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR.getReason(), MIME_TYPE);
	}

	private RenderContent translate(ClientDataError t) {
		byte[] content = translateClientError(t);
		return new RenderContent(content, KnownStatusCode.HTTP_400_BADREQUEST.getCode(), KnownStatusCode.HTTP_400_BADREQUEST.getReason(), MIME_TYPE);
	}

	private CompletableFuture<Action> createNotFoundResponse(Service<MethodMeta, Action> nextFilter, MethodMeta meta) {
		Matcher matcher = pattern.matcher(meta.getCtx().getRequest().relativePath);
		if(!matcher.matches())
			return nextFilter.invoke(meta);
		
		return CompletableFuture.completedFuture(
					createNotFound()
				);
	}

	private Action createNotFound() {
		byte[] content = createNotFoundJsonResponse();		
		return new RenderContent(content, KnownStatusCode.HTTP_404_NOTFOUND.getCode(), KnownStatusCode.HTTP_404_NOTFOUND.getReason(), MIME_TYPE);
	}

	protected abstract byte[] translateServerError(Throwable t);

	protected abstract byte[] translateClientError(ClientDataError t);

	/**
	 * If you really want, return null and the filter will return the 404 html instead of
	 * json if you really want
	 */
	protected abstract byte[] createNotFoundJsonResponse();
	
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
