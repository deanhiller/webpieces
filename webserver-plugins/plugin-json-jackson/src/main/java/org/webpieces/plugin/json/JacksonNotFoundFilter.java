package org.webpieces.plugin.json;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import org.webpieces.util.futures.XFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.webpieces.ctx.api.Current;
import org.webpieces.httpparser.api.dto.KnownStatusCode;
import org.webpieces.router.api.controller.actions.Action;
import org.webpieces.router.api.controller.actions.RenderContent;
import org.webpieces.router.api.routes.MethodMeta;
import org.webpieces.router.api.routes.RouteFilter;
import org.webpieces.router.impl.compression.MimeTypes.MimeTypeResult;
import org.webpieces.util.SneakyThrow;
import org.webpieces.util.filters.Service;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JacksonNotFoundFilter extends RouteFilter<JsonConfig> {

	public static final MimeTypeResult MIME_TYPE = new MimeTypeResult("application/json", StandardCharsets.UTF_8);
	private final ObjectMapper mapper;

	private Pattern pattern;

	@Inject
	public JacksonNotFoundFilter(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	public XFuture<Action> filter(MethodMeta meta, Service<MethodMeta, Action> nextFilter) {
		String path = Current.request().relativePath;
		if(pattern.matcher(path).matches())
			return createNotFoundResponse(nextFilter, meta);
		
		return nextFilter.invoke(meta);
	}

	@Override
	public void initialize(JsonConfig config) {
		this.pattern = config.getFilterPattern();
	}

	protected XFuture<Action> createNotFoundResponse(Service<MethodMeta, Action> nextFilter, MethodMeta meta) {
		Matcher matcher = pattern.matcher(meta.getCtx().getRequest().relativePath);
		if(!matcher.matches())
			return nextFilter.invoke(meta);
		
		return XFuture.completedFuture(
					createNotFound()
				);
	}

	protected Action createNotFound() {
		byte[] content = createNotFoundJsonResponse();		
		return new RenderContent(content, KnownStatusCode.HTTP_404_NOTFOUND.getCode(), KnownStatusCode.HTTP_404_NOTFOUND.getReason(), MIME_TYPE);
	}

	protected byte[] createNotFoundJsonResponse() {
		JsonError error = new JsonError();
		error.setError("This url does not exist.  try another url");
		error.setCode(404);
		return translateJson(mapper, error);
	}

	protected byte[] translateJson(ObjectMapper mapper, Object error) {
		try {
			return mapper.writeValueAsBytes(error);
		} catch (IOException e) {
			throw SneakyThrow.sneak(e);
		}
	}
	
	
}
