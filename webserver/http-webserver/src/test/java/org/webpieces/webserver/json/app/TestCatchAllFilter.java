package org.webpieces.webserver.json.app;

import org.webpieces.plugins.json.JacksonCatchAllFilter;
import org.webpieces.router.api.exceptions.ClientDataError;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.inject.Inject;

public class TestCatchAllFilter extends JacksonCatchAllFilter {

	private ObjectMapper mapper;

	@Inject
	public TestCatchAllFilter(ObjectMapper mapper) {
		super(mapper);
		this.mapper = mapper;
	}

	@Override
	protected byte[] translateClientError(ClientDataError t) {
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
