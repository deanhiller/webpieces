package org.webpieces.webserver.json.app;

import javax.inject.Inject;

import org.webpieces.plugin.json.JacksonCatchAllFilter;
import org.webpieces.router.api.exceptions.HttpException;
import org.webpieces.router.api.exceptions.NotFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestCatchAllFilter extends JacksonCatchAllFilter {

	private ObjectMapper mapper;

	@Inject
	public TestCatchAllFilter(ObjectMapper mapper) {
		super(mapper);
		this.mapper = mapper;
	}

	protected byte[] translateHttpException(HttpException t) {
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
