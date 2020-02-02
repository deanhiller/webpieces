package WEBPIECESxPACKAGE.web.tags;

import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.impl.tags.FieldTag;

public class IdTag extends FieldTag {

	public IdTag(ConverterLookup converter, String fieldHtmlPath) {
		super(converter, fieldHtmlPath);
	}

	@Override
	public String getName() {
		return "id";
	}

}
