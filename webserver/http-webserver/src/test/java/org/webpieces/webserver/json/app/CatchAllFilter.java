package org.webpieces.webserver.json.app;

import java.io.IOException;

import javax.inject.Inject;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.webpieces.plugins.json.JsonCatchAllFilter;
import org.webpieces.router.api.exceptions.ClientDataError;

public class CatchAllFilter extends JsonCatchAllFilter {

	private ObjectMapper mapper;

	@Inject
	public CatchAllFilter(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	protected byte[] translateClientError(ClientDataError t) {
		JsonError error = new JsonError();
		error.setError(t.getMessage());
		error.setCode(0);
		
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

	@Override
	protected byte[] createNotFoundJsonResponse() {
		JsonError error = new JsonError();
		error.setError("This url has no api.  try another url");
		error.setCode(0);
		try {
			byte[] data = mapper.writeValueAsBytes(error);
			return data;
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected byte[] translateServerError(Throwable t) {
		JsonError error = new JsonError();
		error.setError("Server ran into a bug, please report");
		error.setCode(0);
		try {
			byte[] data = mapper.writeValueAsBytes(error);
			return data;
		} catch (JsonGenerationException e) {
			throw new RuntimeException(e);
		} catch (JsonMappingException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
