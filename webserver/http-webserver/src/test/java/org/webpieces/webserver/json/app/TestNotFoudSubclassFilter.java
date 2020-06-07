package org.webpieces.webserver.json.app;

import javax.inject.Inject;

import org.webpieces.plugin.json.JacksonNotFoundFilter;

import com.fasterxml.jackson.databind.ObjectMapper;

public class TestNotFoudSubclassFilter extends JacksonNotFoundFilter {

	private ObjectMapper mapper;

	@Inject
	public TestNotFoudSubclassFilter(ObjectMapper mapper) {
		super(mapper);
		this.mapper = mapper;
	}

	@Override
	protected byte[] createNotFoundJsonResponse() {
		JsonError error = new JsonError();
		error.setError("This url has no api.  try another url");
		error.setCode(0);
		return translateJson(mapper, error);
	}

}
