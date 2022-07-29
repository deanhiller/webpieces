package webpiecesxxxxxpackage.web.tags;

import org.webpieces.templating.api.ConverterLookup;
import org.webpieces.templating.impl.tags.FieldTag;

public class MyFieldTag extends FieldTag {

	public MyFieldTag(ConverterLookup converter) {
		super(converter, "/webpiecesxxxxxpackage/web/tags/myfield.tag");
	}

	@Override
	public String getName() {
		return "myfield";
	}

}
