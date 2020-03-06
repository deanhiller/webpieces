package WEBPIECESxPACKAGE.json;

import javax.inject.Inject;

import org.apache.commons.lang3.StringEscapeUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.webpieces.plugins.json.JacksonCatchAllFilter;
import org.webpieces.router.api.exceptions.ClientDataError;

public class JsonCatchAllFilter extends JacksonCatchAllFilter {

	@Inject
	public JsonCatchAllFilter(ObjectMapper mapper) {
		super(mapper);
	}
	

	
}
