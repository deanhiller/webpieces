package org.webpieces.webserver.json.app;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.webpieces.plugins.json.JacksonCatchAllFilter;
import org.webpieces.router.api.exceptions.ClientDataError;

public class TestCatchAllFilter extends JacksonCatchAllFilter {

	private ObjectMapper mapper;

	@Inject
	public TestCatchAllFilter(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	protected byte[] translateClientError(ClientDataError t) {
		String escapeJson = StringEscapeUtils.escapeJson(t.getMessage());
		JsonError error = new JsonError();
		error.setError(escapeJson);
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
