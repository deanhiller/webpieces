package org.webpieces.plugins.json;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.actions.Action;
import org.webpieces.router.api.actions.RenderContent;
import org.webpieces.router.api.dto.MethodMeta;
import org.webpieces.router.api.exceptions.ClientDataError;
import org.webpieces.router.api.routing.RouteFilter;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.util.filters.Service;
import org.webpieces.util.logging.Logger;
import org.webpieces.util.logging.LoggerFactory;

public abstract class JsonCatchAllFilter extends RouteFilter<Boolean> {

	private static final Logger log = LoggerFactory.getLogger(JsonCatchAllFilter.class);
	private static final MimeTypeResult mimeType = new MimeTypeResult("application/json", StandardCharsets.UTF_8);
	private Boolean isNotFoundFilter;

	@Override
	public CompletableFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		return nextFilter.invoke(meta).handle((a, t) -> translateFailure(a, t));
	}

	@Override
	public void initialize(Boolean isNotFoundFilter) {
		this.isNotFoundFilter = isNotFoundFilter;
	}

	private Action translateFailure(Action action, Throwable t) {
		if(isNotFoundFilter) {
			return createNotFoundResponse();
		} else if(t != null) {
			if(t instanceof ClientDataError) {
				return translate((ClientDataError)t);
			}
			
			log.error("Internal Server Error", t);
			return translateError(t);
		} else {
			return action;
		}
	}

	private RenderContent translateError(Throwable t) {
		byte[] content = translateServerError(t);
		return new RenderContent(content, KnownStatusCode.HTTP_500_INTERNAL_SVR_ERROR, mimeType);
	}

	private RenderContent translate(ClientDataError t) {
		byte[] content = translateClientError(t);
		return new RenderContent(content, KnownStatusCode.HTTP_400_BADREQUEST, mimeType);
	}

	private RenderContent createNotFoundResponse() {
		byte[] content = createNotFoundJsonResponse();
		return new RenderContent(content, KnownStatusCode.HTTP_404_NOTFOUND, mimeType);
	}

	protected abstract byte[] translateServerError(Throwable t);

	protected abstract byte[] translateClientError(ClientDataError t);
	
	protected abstract byte[] createNotFoundJsonResponse();
	
	
	
}
