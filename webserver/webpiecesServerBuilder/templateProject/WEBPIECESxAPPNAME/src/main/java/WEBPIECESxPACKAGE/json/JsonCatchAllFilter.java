package WEBPIECESxPACKAGE.json;

import javax.inject.Inject;

import org.codehaus.jackson.map.ObjectMapper;
import org.webpieces.plugins.json.JacksonCatchAllFilter;

public class JsonCatchAllFilter extends JacksonCatchAllFilter {

	@Inject
	public JsonCatchAllFilter(ObjectMapper mapper) {
		super(mapper);
	}
	

	
}
