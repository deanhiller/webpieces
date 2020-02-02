package WEBPIECESxPACKAGE.json;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.webpieces.plugins.json.JacksonCatchAllFilter;
import org.webpieces.router.api.exceptions.ClientDataError;

public class JsonCatchAllFilter extends JacksonCatchAllFilter {

	private ObjectMapper mapper;

	@Inject
	public JsonCatchAllFilter(ObjectMapper mapper) {
		this.mapper = mapper;
	}
	
	@Override
	protected byte[] translateClientError(ClientDataError t) {
		String escapeJson = StringEscapeUtils.escapeJson(t.getMessage());
		JsonError error = new JsonError();
		error.setError("400 bad request: "+escapeJson);
		error.setCode(0);
		
		return translateJson(mapper, error);
	}

	@Override
	protected byte[] createNotFoundJsonResponse() {
		JsonError error = new JsonError();
		error.setError("404 This url does not exist.  try another url");
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
