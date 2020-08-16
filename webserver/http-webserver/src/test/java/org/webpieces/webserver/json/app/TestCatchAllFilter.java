package org.webpieces.webserver.json.app;

import javax.inject.Inject;

import org.webpieces.plugin.json.JacksonCatchAllFilter;
import org.webpieces.plugin.json.JacksonJsonConverter;
import org.webpieces.router.api.exceptions.BadClientRequestException;
import org.webpieces.router.api.exceptions.HttpException;
import org.webpieces.router.api.exceptions.NotFoundException;
import org.webpieces.router.api.routes.MethodMeta;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestCatchAllFilter extends JacksonCatchAllFilter {

	private JacksonJsonConverter mapper;

	@Inject
	public TestCatchAllFilter(JacksonJsonConverter mapper) {
		super(mapper);
		this.mapper = mapper;
	}

	@Override
	protected byte[] translateHttpException(MethodMeta meta, HttpException t) {
		if(t instanceof NotFoundException)
			return createNotFoundJsonResponse();
		
		JsonError error = new JsonError();
		error.setError(t.getMessage());
		error.setCode(0);
		return translateJson(mapper, error);		
	}

	@Override
	protected byte[] createNotFoundJsonResponse() {
		JsonError error = new JsonError();
		error.setError("This url has no api.  try another url");
		error.setCode(0);
		return translateJson(mapper, error);
	}

	@Override
	protected byte[] translateServerError(Throwable t) {
		JsonError error = new JsonError();
		error.setError("Server ran into a bug, please report");
		error.setCode(0);
		return translateJson(mapper, error);
	}
	
}
