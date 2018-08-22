package WEBPIECESxPACKAGE.base.tags;

import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.impl.tags.FieldTag;

public class MyFieldTag extends FieldTag {

	public MyFieldTag(ConverterLookup converter) {
		super(converter, "/WEBPIECESxPACKAGE/base/tags/myfield.tag");
	}

	@Override
	public String getName() {
		return "myfield";
	}

}
